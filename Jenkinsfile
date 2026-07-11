pipeline {

    agent any

    tools {
        jdk 'Java21'
        maven 'Maven'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {

        SCANNER_HOME = tool 'SonarScanner'

        IMAGE_NAME = "ramakir/java-demo"
        IMAGE_TAG  = "${BUILD_NUMBER}"

        MINIKUBE_SERVER = "ubuntu@172.31.45.144"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/ramakir/jenkins-java21-demo.git'
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean verify'
            }
        }

        stage('SonarQube Analysis') {

            steps {

                withSonarQubeEnv('SonarQube') {

                    sh """
                    ${SCANNER_HOME}/bin/sonar-scanner \
                    -Dsonar.projectKey=java-demo \
                    -Dsonar.sources=src/main \
                    -Dsonar.tests=src/test \
                    -Dsonar.java.binaries=target/classes \
                    -Dsonar.java.libraries=target/*.jar \
                    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    """
                }
            }
        }

        stage('Quality Gate') {

            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: false
                }
            }
        }

        stage('Docker Build') {

            steps {

                sh """
                docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
                """
            }
        }

        stage('Docker Login') {

            steps {

                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {

                    sh '''
                    echo $DOCKER_PASS | docker login \
                    -u $DOCKER_USER \
                    --password-stdin
                    '''
                }
            }
        }

        stage('Push Docker Image') {

            steps {

                sh """
                docker push ${IMAGE_NAME}:${IMAGE_TAG}
                docker push ${IMAGE_NAME}:latest
                """
            }
        }

        stage('Deploy To Kubernetes') {

            steps {

                sshagent(['minikube-ssh']) {

                    sh """

                    echo "Copying Kubernetes manifests..."

                    scp -o StrictHostKeyChecking=no \
                    k8s/deployment.yaml \
                    k8s/service.yaml \
                    ${MINIKUBE_SERVER}:/home/ubuntu/

                    echo "Updating image tag..."

                    ssh -o StrictHostKeyChecking=no ${MINIKUBE_SERVER} "

                    sed -i 's|IMAGE_TAG|${IMAGE_TAG}|g' /home/ubuntu/deployment.yaml

                    kubectl apply -f /home/ubuntu/deployment.yaml

                    kubectl apply -f /home/ubuntu/service.yaml

                    kubectl rollout status deployment/java-demo

                    kubectl get pods
                    kubectl get svc
                    "
                    """
                }
            }
        }
    }

    post {

        success {
            echo "Pipeline Successful"
        }

        failure {
            echo "Pipeline Failed"
        }

        always {

            archiveArtifacts artifacts: 'target/*.jar',
                              fingerprint: true

            junit '**/target/surefire-reports/*.xml'

            sh 'docker logout || true'
        }
    }
}
