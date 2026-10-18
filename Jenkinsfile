pipeline {
    agent any

    parameters {
        string(name: 'TOMCAT_WEBAPPS',
               defaultValue: 'C:\\DevTools\\apache-tomcat-10.1.61\\webapps',
               description: 'Tomcat webapps folder to deploy into')
        string(name: 'APP_CONTEXT',
               defaultValue: 'kyc-portal',
               description: 'Context path of the deployed app (also the WAR name)')
    }

    environment {
        JAVA_HOME = 'C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.12.8-hotspot'
        MVN = 'C:\\DevTools\\apache-maven-3.9.16-bin\\apache-maven-3.9.16\\bin\\mvn.cmd'
        PATH = "C:\\Program Files\\Docker\\Docker\\resources\\bin;C:\\Users\\yo\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin;${env.PATH}"
        DOCKER_HOST = 'npipe:////./pipe/dockerDesktopLinuxEngine'
        IMAGE = 'localhost:5000/kyc-portal'
        VERSION = "1.0.${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat '"%MVN%" -B clean test'
            }
        }

        stage('Package') {
            steps {
                bat '"%MVN%" -B package -DskipTests'
            }
        }

        stage('Selenium Tests') {
            steps {
                bat '"%MVN%" -B test -Pselenium'
            }
        }

        stage('Deploy') {
            steps {
                script {
                    def webapps = params.TOMCAT_WEBAPPS ?: 'C:\\DevTools\\apache-tomcat-10.1.61\\webapps'
                    def context = params.APP_CONTEXT ?: 'kyc-portal'
                    echo "Deploying to ${webapps}\\${context}.war"
                    bat "copy /Y target\\kyc-portal.war \"${webapps}\\${context}.war\""
                }
            }
        }

        stage('Docker Build') {
            steps {
                bat 'docker build -t %IMAGE%:%VERSION% -t %IMAGE%:latest .'
            }
        }

        stage('Docker Push') {
            steps {
                bat 'docker push %IMAGE%:%VERSION%'
                bat 'docker push %IMAGE%:latest'
            }
        }

        stage('Run Container') {
            steps {
                bat 'docker rm -f kyc-portal-ci || exit /b 0'
                bat 'docker run -d --name kyc-portal-ci -p 8091:8085 %IMAGE%:%VERSION%'
            }
        }
        stage('Provision with Ansible') {
            agent { label 'wsl' }
            steps {
                checkout scm
                script {
                    def ws = env.WORKSPACE.replace('\\', '/')
                    def wslWs = "/mnt/" + ws.substring(0, 1).toLowerCase() + ws.substring(2)
                    bat "wsl -d Ubuntu -u root -- bash -lc \"cd '${wslWs}/ansible' && ansible-playbook -i inventory.ini site.yml -e image_tag=${env.VERSION}\""
                }
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'target/screenshots/*.png', allowEmptyArchive: true
        }
        success {
            archiveArtifacts artifacts: 'target/*.war', fingerprint: true
            echo "Tomcat: http://localhost:8082/${params.APP_CONTEXT ?: 'kyc-portal'}/"
            echo "Container: http://localhost:8091/ (image ${env.IMAGE}:${env.VERSION})"
            echo "Ansible node: http://localhost:8092/ (image ${env.IMAGE}:${env.VERSION})"
        }
    }
}