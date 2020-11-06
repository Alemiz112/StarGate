pipeline {
    agent any
    tools {
        maven 'Maven 3'
        jdk 'Java 11'
    }
    options {
        buildDiscarder(logRotator(artifactNumToKeepStr: '15'))
    }
    stages {
        stage ('Build') {
            steps {
                withMaven(options: [pipelineGraphPublisher(lifecycleThreshold: 'install')]) {
                    sh 'mvn clean install'
                }
            }
        }

        stage('Snapshot') {
            when {
                branch "develop"
            }
            steps {
                sh 'mvn source:jar deploy -DskipTests'
            }
        }

        stage ('Release') {
            when {
                branch "master"
            }
            steps {
                sh 'mvn javadoc:jar source:jar deploy -DskipTests'
            }
        }

    }
    post {
        always {
            deleteDir()
        }
    }
}