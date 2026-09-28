pipeline {
    agent any

    environment {

        IMAGE_TAG = "${env.BUILD_NUMBER}"
        PREVIOUS_TAG = "${env.BUILD_NUMBER.toInteger() - 1}"
        PROJECT_NAME = "petclinic"
        PORT_INTERNAL = "8080"
        PORT_EXTERNAL = "8089"
    }

    stages {
        stage('Clone Repository') {
            steps {
                echo "Clone React Job Portal Frontend from Main Branch"

                git branch: 'main', url: 'https://github.com/9mAhmad/spring-petclinic-pipeline-jenkins.git'

                sh 'ls -lah'
            }
        }
        stage('Secrets detection...') {
            steps {
                    script {
                        def rc = sh(script: 'betterleaks dir . --report-path betterleaks.json --report-format json', returnStatus: true)
                        if (rc != 0) {
                            unstable("betterleaks found potential secrets (exit ${rc})")
                        }

                        archiveArtifacts artifacts: 'betterleaks.*', fingerprint: true

                    }
 
            }
        }
        stage('SAST Scanning...') {
            steps {
                    sh "semgrep scan --config auto --json --output=semgrep.json"

                    archiveArtifacts artifacts: 'semgrep.*', fingerprint: true
            }
        }
        // stage('Trivy FS Scan...') { 
        //     steps {
        //         sh ''' 
        //             trivy fs . \\
        //                 --scanners vuln,misconfig,secret \\
        //                 --skip-files 'betterleaks.json,semgrep.json,.pkgjson.md5' \\
        //                 --format json -o trivy-fs-report.json

        //             trivy convert \\
        //                 --format template --template "@/vagrant_shared/html.tpl" \\
        //                 -o trivy-fs-report.html trivy-fs-report.json
        //         '''

        //         archiveArtifacts artifacts: 'trivy-fs-report.*', fingerprint: true
        //     }
        // }
        
        stage('Build Docker Image...') {
            steps {
                script {
                    echo "🔨 Building Docker image with Buildx..."

                    sh """
                        # Ensure buildx is available
                        docker --version

                        docker build -t ${env.PROJECT_NAME}:v${env.IMAGE_TAG} .

                        echo "✅ Docker image built successfully"

                        docker images -a
                    """
                }
            }
        }
        // stage('Trivy Image Scan...') {
        //     steps {

        //         sh '''
        //             trivy image \\
        //                 --scanners vuln,misconfig,secret \\
        //                 --skip-files '**/betterleaks.json,**/semgrep.json' \\
        //                 --format json -o trivy-image-report.json "${PROJECT_NAME}:v${IMAGE_TAG}"

        //             trivy convert \\
        //                 --format template --template "@/vagrant_shared/html.tpl" \\
        //                 -o trivy-image-report.html trivy-image-report.json

        //         '''

        //         archiveArtifacts artifacts: 'trivy-image-report.*', fingerprint: true
        //     }
        // }
        stage('Run our Backend') {
            steps {
                echo "Deleting previous running container -- ${env.PROJECT_NAME}-v${env.PREVIOUS_TAG}"
                sh "docker rm -f ${env.PROJECT_NAME}-v${env.PREVIOUS_TAG}"
                sh "docker run -e SPRING_PROFILES_ACTIVE=mysql -d -p ${PORT_EXTERNAL}:${env.PORT_INTERNAL} --name ${env.PROJECT_NAME}-v${env.IMAGE_TAG} ${env.PROJECT_NAME}:v${env.IMAGE_TAG}"
                sleep(10)
            }
        }
        stage('Check our Backend Container running status log') {
            steps {
                sh "docker logs ${env.PROJECT_NAME}-v${env.IMAGE_TAG}"
            }
        }
    }

    post {
        success {
            echo "🎉 Deployment successful! I am from success block"
            
            emailext(
                to: "${ALERT_EMAIL}",
                subject: "Jenkins Build SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                mimeType: 'text/html',
                body: """
                    <p>Build <b>${env.JOB_NAME} #${env.BUILD_NUMBER}</b> failed.</p>
                    <p>Console: <a href="${env.BUILD_URL}console">${env.BUILD_URL}console</a></p>
                    <p>The full console log is attached.</p>
                """,
                attachLog: true,     // attaches build.log
                compressLog: false   // plain text, not .gz
            )
        }

        failure {
            echo "❌ Deployment failed! I am from failure block"

            emailext(
                to: "${ALERT_EMAIL}",
                subject: "Jenkins Build FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                mimeType: 'text/html',
                body: """
                    <p>Build <b>${env.JOB_NAME} #${env.BUILD_NUMBER}</b> failed.</p>
                    <p>Console: <a href="${env.BUILD_URL}console">${env.BUILD_URL}console</a></p>
                    <p>The full console log is attached.</p>
                """,
                attachLog: true,     // attaches build.log
                compressLog: false   // plain text, not .gz
            )
        }

        always {
            echo "🎉 Hey! I am always block"
            // sh "rm -f ${APP_NAME}-${IMAGE_TAG}.tar || true"
            // cleanWs()
        }
    }
}
  