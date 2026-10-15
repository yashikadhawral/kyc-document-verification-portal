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
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
        }
        success {
            archiveArtifacts artifacts: 'target/*.war', fingerprint: true
            echo "Deployed: http://localhost:8082/${params.APP_CONTEXT ?: 'kyc-portal'}/"
        }
    }
}