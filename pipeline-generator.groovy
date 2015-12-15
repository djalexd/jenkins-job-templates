job("${prefix}-start") {
  deliveryPipelineConfiguration("start")
  steps {
    downstreamParameterized {
      trigger("${prefix}-unit-tests")
    }
  }
}

job("${prefix}-unit-tests") {
  deliveryPipelineConfiguration("build", "unit-tests")
  steps {
    shell("echo 'run unit tests'")
    downstreamParameterized {
      trigger("${prefix}-integration-tests")
    }
  }
}

job("${prefix}-integration-tests") {

  deliveryPipelineConfiguration("build", "integration-tests")
  steps {
    shell("echo 'run integration tests'")
    downstreamParameterized {
      trigger("${prefix}-coverage")
      trigger("${prefix}-deploy-default-env")
    }
  }
}

job("${prefix}-coverage") {

  deliveryPipelineConfiguration("build", "jacoco-coverage")
  steps {
    shell("echo 'run jacoco code coverage'")
  }
}

job("${prefix}-deploy-default-env") {
  deliveryPipelineConfiguration("deploy", "deploy-to-default-env")
  steps {
    shell("echo 'deploy to default environment'")
    downstreamParameterized {
      trigger("${prefix}-done")
    }
  }
}

job("${prefix}-done") {
  deliveryPipelineConfiguration("done")
}

deliveryPipelineView("${prefix}-view") {
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
        regex(/(.*)-start/)
    }
}
