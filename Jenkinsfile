pipeline {
    agent any

    stages {
        stage('Clone Repository') {
            steps {
                git branch: 'dev', url: 'https://github.com/SIMJIYEON93/ci-cd.git'
            }
        }

        stage('Build') {
            steps {
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
                sshagent(['git']) {
                    script {
                        try {
                            sh """
                            scp -o StrictHostKeyChecking=no build/libs/ci-cd-0.0.1-SNAPSHOT.jar ubuntu@ec2-3-38-214-141.ap-northeast-2.compute.amazonaws.com:/home/ubuntu/
                            ssh ubuntu@ec2-3-38-214-141.ap-northeast-2.compute.amazonaws.com 'pkill -f "java -jar" || true'
                            ssh ubuntu@ec2-3-38-214-141.ap-northeast-2.compute.amazonaws.com 'nohup java -jar /home/ubuntu/ci-cd-0.0.1-SNAPSHOT.jar --server.port=8080 &'
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
            // 전체 빌드 오류 로그 추가
            script {
                def log = currentBuild.rawBuild.getLog(20).join("\n")
                echo "Last 20 lines of build log:\n${log}"
            }
        }
    }
}
