pipeline {
    agent{
        node{
            label 'jenkins-jenkins-slave'
        }
    }
            tools {
                maven 'maven'
                jdk 'jdk'
            } 
    stages {
        stage ('Git Checkout') {
            steps{
           git 'https://github.com/fhause5/petclinic-be.git'
            }
        }
        stage ('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                    echo "JAVA_HOME = ${JAVA_HOME}"
                ''' 
            }
        }
        stage ('Build') {
            steps {
                sh 'mvn -Dmaven.test.failure.ignore=true install'
            }
            post {
                success {
                    junit 'target/surefire-reports/**/*.xml'
                }
            }
        }
        stage ('SonarQube analysis') {
            steps {
        
              withSonarQubeEnv('sonarqube') {
                sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.6.0.1398:sonar'
        }
        timeout(time: 100, unit: 'MINUTES') {
            waitForQualityGate abortPipeline: true
        }
    }
}

            

        }
}