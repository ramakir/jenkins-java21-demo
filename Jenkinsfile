pipeline {

    agent any

    tools {
        jdk 'Jdk21'
        maven 'Maven'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        SCANNER_HOME = tool 'SonarScanner'
    }

    stages {

        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Unit Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package'
            }
        }

        stage('SonarQube Analysis') {

            steps {

                withSonarQubeEnv('SonarQube') {

                    sh """
                    ${SCANNER_HOME}/bin/sonar-scanner \
                    -Dsonar.projectKey=java-demo \
                    -Dsonar.sources=src \
                    -Dsonar.java.binaries=target/classes
                    """
                }
            }
        }

        stage('Quality Gate') {

            steps {

                timeout(time: 10, unit: 'MINUTES') {

                    waitForQualityGate abortPipeline: true

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
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            junit '**/target/surefire-reports/*.xml'
        }

    }

}
