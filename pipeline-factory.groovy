

 def guessProjectName(gitUrl) {
  def index1 = gitUrl.indexOf("github.com")
  if (index1 < 0) {
    throw new IllegalArgumentException(gitUrl + " not recognized");
  } else {
    def subString1 = gitUrl.substring(index1 + "github.com".length() + 1);
    subString1.substring(0, subString1.length() - 4);
  }
}

def project = guessProjectName("${git_scm_url}")
def branchApi = new URL("https://api.github.com/repos/${project}/branches")
def branches = new groovy.json.JsonSlurper().parse(branchApi.newReader())
branches.each {
    def branchName = it.name
    def jobName = "${job_base_name} - Unit Tests - ${branchName}".replaceAll('/','-')
    job(jobName) {

        label("swarm")

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
        triggers {
          scm('*/5 * * * *')
        }
        steps {
            maven("clean test")
        }
    }
}
