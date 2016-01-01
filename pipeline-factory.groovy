
import utilities.Github

def gitHutProjectName = Github.extractGitHubProjectName("${git_scm_url}")
def branchApi = new URL("https://api.github.com/repos/${gitHutProjectName}/branches")
def branches = new groovy.json.JsonSlurper().parse(branchApi.newReader())
branches.each {
    def branchName = it.name
    /* Configure all job names here for easier referencing */
    List<String> jobNames = [
        "${project}/${branchName}/start",
        "${project}/${branchName}/unit-tests",
        "${project}/${branchName}/integration-tests",
        "${project}/${branchName}/sonar-analysis",
        "${project}/${branchName}/nexus-deploy",
        "${project}/${branchName}/done"
    ] as String[]
    
    def sanitizedJobNames = jobNames
    
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
        deliveryPipelineConfiguration("build", "sonar-analysis")
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
        
        /*publishers {
            sonar {
                branch(branchName)
                overrideTriggers {
                    skipIfEnvironmentVariable('SKIP_SONAR')
                }
            }
        }*/

        steps {
            maven("clean install")
            maven("sonar:sonar -Dsonar.host.url=http://sonar:9000")
            downstreamParameterized {
                trigger(sanitizedJobNames.get(4)) {
                    parameters {
                        currentBuild()
                    }
                }
            }
        }
    }
    
    job(sanitizedJobNames.get(4)) {
        deliveryPipelineConfiguration("done")
    }

    deliveryPipelineView("${project}-${branch}-view") {
        pipelineInstances(5)
        showAggregatedPipeline()
        columns(2)
        updateInterval(2)
        enableManualTriggers()
        showAvatars()
        showChangeLog()
        pipelines {
            /*component('Sub System A', 'compile-a')
            component('Sub System B', 'compile-b')*/
            // ${project} - start - ${branchName}
            regex(/(.*)-start/)
        }
    }

}
