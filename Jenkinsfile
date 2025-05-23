pipeline {
    agent any

    environment {
        CONFIG_FILE = 'config.csv'
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
                            env.IMAGE_PATH = parts[2].trim() // e.g., myorg/myimage:tag or AWS ECR URL
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

        stage('Pull Image') {
            steps {
                script {
                    echo "Pulling image: ${env.IMAGE_PATH}"

                    if (env.IMAGE_PATH.contains(".dkr.ecr.")) {
                        def ecrRegistry = env.IMAGE_PATH.split('/')[0]
                        echo "Detected ECR image. Authenticating to ECR: ${ecrRegistry}"

                        sh """
                        aws ecr get-login-password --region ${env.REGION} | docker login --username AWS --password-stdin ${ecrRegistry}
                        docker pull ${env.IMAGE_PATH}
                        """
                    } else {
                        sh "docker pull ${env.IMAGE_PATH}"
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
                    # Uncomment the line below to enable real deployment
                    # kubectl set image deployment/${env.DEPLOY_NAME} ${env.CONTAINER_NAME}=${env.IMAGE_PATH} --namespace=default
                    """
                }
            }
        }
    }
}
