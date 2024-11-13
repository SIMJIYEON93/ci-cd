pipeline {
    agent any

    environment {
        EC2_HOST = 'ec2-3-38-193-202.ap-northeast-2.compute.amazonaws.com'
        JAR_NAME = 'ci-cd-0.0.1-SNAPSHOT.jar'
    }

    stages {
        stage('Workspace Clean') {
            steps {
                echo "Cleaning workspace..."
                cleanWs()
            }
        }

        stage('Clone Repository') {
            steps {
                script {
                    echo "Starting Clone Repository stage"
                    sh 'pwd && ls -la'
                    sh 'git --version'

                    checkout([$class: 'GitSCM',
                        branches: [[name: '*/dev']],
                        userRemoteConfigs: [[
                            credentialsId: 'ssh',  // SSH 인증에 사용될 SSH 키 ID
                            url: 'git@github.com:SIMJIYEON93/ci-cd.git'
                        ]]
                    ])
                }
            }
        }

        stage('Prepare Build') {
            steps {
                script {
                    echo "Preparing build environment..."
                    sh 'chmod +x gradlew'
                    sh './gradlew clean build -x test --stacktrace --info'
                }
            }
        }

        stage('Deploy') {
            steps {
                echo "Starting Deploy stage"
                withCredentials([file(credentialsId: 'cd', variable: 'AWS_PEM_FILE')]) {
                    script {
                        try {
                            echo "Testing SSH connection to EC2..."
                            sh """
                                ssh -i ${AWS_PEM_FILE} -o StrictHostKeyChecking=no ubuntu@${EC2_HOST} 'echo SSH Connection successful'
                            """

                            echo "Transferring JAR file to EC2..."
                            sh """
                                scp -i ${AWS_PEM_FILE} -o StrictHostKeyChecking=no build/libs/${JAR_NAME} ubuntu@${EC2_HOST}:/home/ubuntu/app/
                            """

                            echo "Running deployment script on EC2..."
                            sh """
                                ssh -i ${AWS_PEM_FILE} -o StrictHostKeyChecking=no ubuntu@${EC2_HOST} '''
                                    # Java 버전 확인
                                    java -version

                                    # 기존 프로세스 종료
                                    pkill -f "${JAR_NAME}" || true

                                    # JAR 파일 권한 수정 (실행 권한 추가)
                                    chmod +x /home/ubuntu/app/${JAR_NAME}

                                    # 새 버전 실행
                                    nohup java -jar /home/ubuntu/app/${JAR_NAME} > /home/ubuntu/application.log 2>&1 &

                                    # 프로세스 실행 확인
                                    sleep 20
                                    if pgrep -f "${JAR_NAME}"; then
                                        echo "Application started successfully"
                                    else
                                        echo "Failed to start application"
                                        exit 1
                                    fi
                                '''
                            """
                        } catch (Exception e) {
                            echo "Deployment failed with error: ${e.getMessage()}"
                            currentBuild.result = 'FAILURE'
                            error "Stopping pipeline due to deployment error"
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline execution completed'
        }
        success {
            echo 'Pipeline succeeded!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
