package utilities

class Github {
    /**
     * Extracts the project name, based on github project url:
     * i.e.
     *   git@github.com:test1/proj.git => test1/proj
     *   https://github.com/test1/proj.git => test1/proj
     */
    static def extractGitHubProjectName(def githubProjectUrl) {
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

}