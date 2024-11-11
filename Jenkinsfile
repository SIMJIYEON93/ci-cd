pipeline {
    agent any

    stages {
        stage('Clone Repository') {
            steps {
                echo "Starting Clone Repository stage"
                git branch: 'dev', url: 'https://github.com/SIMJIYEON93/ci-cd.git'
            }
        }

        stage('Build') {
            steps {
                echo "Starting Build stage"
                script {
                    try {
                        sh './gradlew clean build'
                    } catch (Exception e) {
                        echo "Build failed with error: ${e.getMessage()}"
                        error "Stopping pipeline due to build error"
                    }
                }
            }
        }

        stage('Test') {
            steps {
                echo "Starting Test stage"
                script {
                    try {
                        sh './gradlew test'
                    } catch (Exception e) {
                        echo "Test failed with error: ${e.getMessage()}"
                        error "Stopping pipeline due to test error"
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                echo "Starting Deploy stage"
                sshagent(['git']) {
                    script {
                        try {
                            sh """
                            scp -o StrictHostKeyChecking=no build/libs/ci-cd-0.0.1-SNAPSHOT.jar ubuntu@ec2-3-38-214-141.ap-northeast-2.compute.amazonaws.com:/home/ubuntu/
                            ssh ubuntu@ec2-3-38-214-141.ap-northeast-2.compute.amazonaws.com 'pkill -f "java -jar" || true'
                            ssh ubuntu@ec2-3-38-214-141.ap-northeast-2.compute.amazonaws.com 'nohup java -jar /home/ubuntu/ci-cd-0.0.1-SNAPSHOT.jar &'
                            """
                        } catch (Exception e) {
                            echo "Deployment failed with error: ${e.getMessage()}"
                            error "Stopping pipeline due to deployment error"
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Build and deployment completed.'
        }
        success {
            echo 'Deployment succeeded!'
        }
        failure {
            echo 'Deployment failed.'
            script {
                def log = currentBuild.rawBuild.getLog(20).join("\n")
                echo "Last 20 lines of build log:\n${log}"
            }
        }
    }
}
