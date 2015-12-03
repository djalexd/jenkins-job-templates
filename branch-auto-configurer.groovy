job("${job_base_name} Branch Auto-Configurer") {
  
  /* Do it every 5 minutes */
  triggers {
  	cron("*/5 * * * *")
  }
  
  /* Call another build (pipeline-factory.xml) with these parameters */
  steps {
    downstreamParameterized {
      
      trigger('pipeline-factory.xml', 'ALWAYS', true,
                    [buildStepFailure: 'FAILURE',
                     failure         : 'FAILURE',
                     unstable        : 'UNSTABLE']) {
                predefinedProp('git_scm_url', "${git_scm_url}")
                predefinedProp('project', "${project}")
            }
    }
  }
}
