
def branchApi = new URL("https://api.github.com/repos/${project}/branches")
def branches = new groovy.json.JsonSlurper().parse(branchApi.newReader())
branches.each {
    println "Found branch ${it.name}"
    job("${project} - Unit Tests - ${it.name}".replaceAll('/','-')) {
        /* the swarm label is a future extension */
        /*label("swarm")*/
        scm {
          git {
            remote {
              url "${git_scm_url}"
            }
            branch "${it.name}"
            createTag false
            clean true
          }
        }
        steps {
            maven("clean test")
        }
    }
    
    job("${project} - Integration Tests - ${it.name}".replaceAll('/','-')) {
        scm {
           git {
              remote {
                 url "${git_scm_url}"
              }
              branch "${it.name}
              createTag false
              clean true
           }
        }
        steps {
           maven("clean verify")
        }
    }
}
