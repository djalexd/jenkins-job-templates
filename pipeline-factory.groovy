/**
 * Extracts the project name, based on github project url:
 * i.e.
 *   git@github.com:test1/proj.git => test1/proj
 *   https://github.com/test1/proj.git => test1/proj
 */ 
​def extractGitHubProjectName(String githubProjectUrl) {    
    def lowerCase = githubProjectUrl.toLowerCase(), 
        length = lowerCase.length(), 
        idx1 = lowerCase.indexOf("github.com"),
        idx2 = idx1 + "github.com".length() + 1,
        idx3 = lowerCase.lastIndexOf(".git")
    if (idx1 < 0) {
        throw new IllegalArgumentException(String.format("Illegal GitHub project url: %s", githubProjectUrl))
    }
    if (idx3 < 0) {
        lowerCase.substring(idx2, lowerCase.length())
    } else {
        lowerCase.substring(idx2, idx3)
    }
}

def gitHutProjectName = extractGitHubProjectName("${git_scm_url}")
def branchApi = new URL("https://api.github.com/repos/${gitHutProjectName}/branches")
def branches = new groovy.json.JsonSlurper().parse(branchApi.newReader())
branches.each {
    def branchName = it.name
    /* Configure all job names here for easier referencing */
    List<String> jobNames = [
        "${project} - start - ${branchName}",
        "${project} - unit-tests - ${branchName}",
        "${project} - integration-tests - ${branchName}",
        "${project} - Sonar analysis - ${branchName}"
        "${project} - done - ${branchName}"
    ] as String[]
    
    def sanitizedJobNames = jobNames.collect { it.replaceAll('/', '-') }
    
    job(sanitizedJobNames.get(0)) {
        /* Do nothing, this is just a placeholder */
        deliveryPipelineConfiguration("start")
        
        steps {
            downstreamParameterized {
                trigger(sanitizedJobNames.get(1)) {
                    parameters {
                        currentBuild()
                    }
                }
            }
        }
    }
    
    job(sanitizedJobNames.get(1)) {
        /* the swarm label is a future extension */
        /*label("swarm")*/
        deliveryPipelineConfiguration("build", "unit-tests")
        scm {
          git {
            remote {
              url "${git_scm_url}"
            }
            branch branchName
            createTag false
            clean true
          }
        }
        steps {
            maven("clean test")
            downstreamParameterized {
                trigger(sanitizedJobNames.get(2)) {
                    parameters {
                        currentBuild()
                    }
                }
            }
        }
    }
    
    job(sanitizedJobNames.get(2)) {
        deliveryPipelineConfiguration("build", "integration-tests")
        scm {
           git {
              remote {
                 url "${git_scm_url}"
              }
              branch branchName
              createTag false
              clean true
           }
        }
        steps {
            maven("clean verify")
            downstreamParameterized {
                trigger(sanitizedJobNames.get(3)) {
                    parameters {
                        currentBuild()
                    }
                }
            }
        }
    }
    
    job(sanitizedJobNames.get(3)) {
        deliveryPipelineConfiguration("done")
    }
}
