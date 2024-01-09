pipeline {
    agent any
    environment {
        GITLAB_CREDENTIALS  = credentials('uabass')
    }
    options {
        preserveStashes()
    }
    tools {
        maven 'maven-3.8'
        jdk 'java-17'
    }
    stages {
        stage('Build') {

            steps {
                git branch: 'main',
                    credentialsId: 'uabass',
                    url: 'https://gitlab.com/teamdigicore/automata-backoffice-lib.git'
                echo 'Building Automata Data Lib'
                configFileProvider([configFile(fileId: 'd8104a7b-cd35-46ea-82ee-b5870ee44981', variable: 'MAVEN_SETTINGS_XML')]) {
                    // sh 'unset JAVA_HOME'
                    sh './mvnw -U clean package deploy -DskipTests -s $MAVEN_SETTINGS_XML'
                }

                archiveArtifacts artifacts: '**/*.jar'
                stash includes: '**/*.jar', name: 'jarFile'
            }

        }
    }
    post {

        success {
            emailext to: "uabass@digicoreltd.com,togunwuyi@digicoreltd.com",
            subject: "jenkins build:${currentBuild.currentResult}: ${env.JOB_NAME}",
            body: "${currentBuild.currentResult}: Job ${env.JOB_NAME}\nMore Info can be found here: ${env.BUILD_URL}",
            attachmentsPattern: '*.jar'
            cleanWs()
        }
        failure {
            emailext to: "uabass@digicoreltd.com,togunwuyi@digicoreltd.com",
            subject: "jenkins build:${currentBuild.currentResult}: ${env.JOB_NAME}",
            body: "${currentBuild.currentResult}: Job ${env.JOB_NAME}\nMore Info can be found here: ${env.BUILD_URL}"
            cleanWs()
        }
    }
}