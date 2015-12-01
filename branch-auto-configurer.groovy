job("${job_base_name} Branch Auto-Configurer") {
  
  /* Do it every 5 minutes */
  triggers {
  	cron("*/5 * * * *")
  }
  
  /* Call another build (dsl_prototype.xml) with these parameters */
  steps {
    downstreamParameterized {
      
      trigger('pipeline-factory.xml', 'ALWAYS', true,
                    [buildStepFailure: 'FAILURE',
                     failure         : 'FAILURE',
                     unstable        : 'UNSTABLE']) {
                predefinedProp('git_scm_url', "${git_scm_url}")
                predefinedProp('job_base_name', "${job_base_name}")
            }
    }
  }
}
