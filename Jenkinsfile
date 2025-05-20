// @Library('your-shared-library') _  // Optional if using shared libs

pipeline {
    agent any

    environment {
        CONFIG_FILE = 'config.csv'
        // PIPELINE_CONFIG = 'pipeline-config.yml'
        ACTIVE_STAGES = 'build, scan, deploy'
    }

    stages {
        stage('Read Config') {
            steps {
                script {
                    def currentBranch = env.BRANCH_NAME ?: 'main'
                    echo "Reading config for branch: ${currentBranch}"

                    def configFound = false
                    def csvFile = readFile(env.CONFIG_FILE).split('\n')
                    for (line in csvFile) {
                        if (line.trim().startsWith(currentBranch + ',')) {
                            def parts = line.split(',')
                            env.IMAGE_EXISTS = parts[1].trim()
                            env.IMAGE_PATH = parts[2].trim()
                            env.DEST_CLUSTER = parts[3].trim()
                            env.CONTAINER_NAME = parts[4].trim()
                            env.DEPLOY_NAME = parts[5].trim()
                            env.REGION = parts[6].trim()
                            configFound = true
                            break
                        }
                    }

                    if (!configFound) {
                        error "No matching config found for branch: ${currentBranch}"
                    }

                    echo "Image Exists: ${env.IMAGE_EXISTS}"
                    echo "Image Path: ${env.IMAGE_PATH}"
                    echo "Cluster: ${env.DEST_CLUSTER}"
                    echo "Deployment: ${env.DEPLOY_NAME}"
                }
            }
        }

        // stage('Load Pipeline Config') {
        //     steps {
        //         script {
        //             def configText = readFile(env.PIPELINE_CONFIG)
        //             def pipelineConfig = new org.yaml.snakeyaml.Yaml().load(configText)
        //             env.ACTIVE_STAGES = pipelineConfig.stages.join(',')
        //             echo "Active Stages: ${env.ACTIVE_STAGES}"
        //         }
        //     }
        // }

            stages {
                    stage('Run Pipeline Stages') {
                        steps {
                            script {
                                def stages = env.ACTIVE_STAGES.split(',').collect { it.trim() }

                                for (stageName in stages) {
                                    def yamlPath = "templates/${stageName}.yaml"
                                    echo "Reading: ${yamlPath}"

                                    if (!fileExists(yamlPath)) {
                                        error "YAML file not found: ${yamlPath}"
                                    }

                                    def yamlContent = readFile(yamlPath)
                                    def parsedYaml = new org.yaml.snakeyaml.Yaml().load(yamlContent)

                                    // Safely get steps from parsed YAML
                                    def commands = parsedYaml?.steps
                                    if (!commands) {
                                        error "No 'steps' defined in: ${yamlPath}"
                                    }

                                    stage("Step: ${stageName}") {
                                        for (cmd in commands) {
                                            sh "${cmd}"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        stage('Deploy') {
            when {
                expression { env.IMAGE_EXISTS == 'yes' }
            }
            steps {
                script {
                    echo "Deploying ${env.CONTAINER_NAME} with image ${env.IMAGE_PATH}"
                    sh """
                    echo "kubectl set image deployment/${env.DEPLOY_NAME} ${env.CONTAINER_NAME}=${env.IMAGE_PATH} --namespace=default"
                    # Actual deploy command:
                    # kubectl set image deployment/${env.DEPLOY_NAME} ${env.CONTAINER_NAME}=${env.IMAGE_PATH} --namespace=default
                    """
                }
            }
        }
    }
}
