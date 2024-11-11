pipeline {
    agent any

    environment {
        // EC2 정보 환경변수로 설정
        EC2_HOST = 'ubuntu@ec2-3-38-214-141.ap-northeast-2.compute.amazonaws.com'
        JAR_NAME = 'ci-cd-0.0.1-SNAPSHOT.jar'
    }

    stages {
        stage('Clone Repository') {
            steps {
                echo "Starting Clone Repository stage"
                // git 문법 수정
                git branch: 'dev', url: 'https://github.com/SIMJIYEON93/ci-cd.git'
            }
        }

        stage('Build') {
            steps {
                echo "Starting Build stage"
                // gradlew 실행 권한 추가
                sh 'chmod +x ./gradlew'
                script {
                    try {
                        sh './gradlew clean build -x test'
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
                            // 배포 스크립트 개선
                            sh """
                                # JAR 파일 전송
                                scp -o StrictHostKeyChecking=no build/libs/${JAR_NAME} ${EC2_HOST}:/home/ubuntu/

                                # 실행 중인 애플리케이션 종료
                                ssh -o StrictHostKeyChecking=no ${EC2_HOST} 'pkill -f "${JAR_NAME}" || true'

                                # 새 버전 실행
                                ssh -o StrictHostKeyChecking=no ${EC2_HOST} '
                                    nohup java -jar /home/ubuntu/${JAR_NAME} > application.log 2>&1 &
                                    sleep 5
                                    if ! pgrep -f "${JAR_NAME}" > /dev/null; then
                                        echo "Application failed to start"
                                        exit 1
                                    fi
                                '
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
                def log = currentBuild.rawBuild.getLog(20).join('\n')
                echo "Last 20 lines of build log:\n${log}"
            }
        }
    }
}