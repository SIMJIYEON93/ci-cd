pipeline {
    agent any

    environment {
        EC2_HOST = 'ec2-13-124-215-135.ap-northeast-2.compute.amazonaws.com'
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
                            credentialsId: 'ssh',
                            url: 'git@github.com:SIMJIYEON93/ci-cd.git'
                        ]])
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
                withCredentials([file(credentialsId: 'ec2', variable: 'AWS_PEM_FILE')]) {
                    script {
                        try {
                            // EC2 연결 테스트
                            echo "Testing SSH connection to EC2..."
                            sh """
                                ssh -i ${AWS_PEM_FILE} -o StrictHostKeyChecking=no ubuntu@${EC2_HOST} 'echo SSH Connection successful'
                            """

                            // JAR 파일 존재 확인
                            echo "Checking if JAR file exists..."
                            sh "ls -l build/libs/${JAR_NAME}"

                            // JAR 파일 전송
                            echo "Transferring JAR file to EC2..."
                            sh """
                                scp -i ${AWS_PEM_FILE} -o StrictHostKeyChecking=no build/libs/${JAR_NAME} ubuntu@${EC2_HOST}:/home/ubuntu/
                            """

                            // 배포 스크립트 실행 (로그를 자세히 확인)
                            echo "Running deployment script on EC2..."
                            sh """
                                ssh -i ${AWS_PEM_FILE} -o StrictHostKeyChecking=no ubuntu@${EC2_HOST} '''
                                    # Java 버전 확인
                                    java -version

                                    # 기존 프로세스 종료
                                    pkill -f "${JAR_NAME}" || true

                                    # JAR 파일 권한 수정 (실행 권한 추가)
                                    chmod +x /home/ubuntu/${JAR_NAME}

                                    # 새 버전 실행
                                    nohup java -jar /home/ubuntu/${JAR_NAME} > /home/ubuntu/application.log 2>&1 &

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
