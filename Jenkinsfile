pipeline {
    agent any

    environment {
        EC2_HOST = 'ubuntu@ec2-13-124-215-135.ap-northeast-2.compute.amazonaws.com'
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
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [],
                        submoduleCfg: [],
                        userRemoteConfigs: [[
                            credentialsId: 'ssh',  // SSH 인증에 사용될 SSH 키 ID
                            url: 'git@github.com:SIMJIYEON93/ci-cd.git'
                        ]]
                    ])

                    sh 'ls -la'
                }
            }
        }

        stage('Prepare Build') {
            steps {
                script {
                    echo "Preparing build environment..."
                    sh 'ls -la gradlew || echo "Gradle wrapper not found"'

                    sh '''
                        if [ -f gradlew ]; then
                            chmod +x gradlew
                            echo "Gradle wrapper permissions set"
                        else
                            echo "Error: gradlew file not found"
                            exit 1
                        fi
                    '''
                }
            }
        }

        stage('Build') {
            steps {
                echo "Starting Build stage"
                script {
                    try {
                        sh './gradlew --version'
                        sh './gradlew clean build -x test --stacktrace --info'
                        sh 'ls -la build/libs/'
                    } catch (Exception e) {
                        echo "Build failed with error: ${e.getMessage()}"
                        currentBuild.result = 'FAILURE'
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
                        sh './gradlew test --stacktrace --info'
                    } catch (Exception e) {
                        echo "Test failed with error: ${e.getMessage()}"
                        currentBuild.result = 'FAILURE'
                        error "Stopping pipeline due to test error"
                    }
                }
            }
        }
//
        stage('Deploy') {
            steps {
                echo "Starting Deploy stage"
                sshagent(credentials: ['aws_ec2']) {  // EC2 접근에 사용될 PEM 키 ID
                    script {
                        try {
                            sh """
                                # 배포 전 EC2 연결 테스트
                                ssh -o StrictHostKeyChecking=no ${EC2_HOST} 'echo "SSH Connection successful"'

                                # JAR 파일 존재 확인
                                ls -l build/libs/${JAR_NAME}

                                # JAR 파일 전송
                                scp -o StrictHostKeyChecking=no build/libs/${JAR_NAME} ${EC2_HOST}:/home/ubuntu/

                                # 배포 스크립트 실행
                                ssh -o StrictHostKeyChecking=no ${EC2_HOST} '''
                                    # Java 버전 확인
                                    java -version

                                    # 기존 프로세스 종료
                                    pkill -f "${JAR_NAME}" || true

                                    # 새 버전 실행
                                    nohup java -jar /home/ubuntu/${JAR_NAME} > application.log 2>&1 &

                                    # 프로세스 실행 확인
                                    sleep 10
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
        }//
    }

    post {
        always {
            echo 'Pipeline execution completed'
            sh 'pwd && ls -la'
        }
        success {
            echo 'Pipeline succeeded!'
        }
        failure {
            echo 'Pipeline failed!'
            echo "Please check the console output for details on the failure."
        }
    }
}
