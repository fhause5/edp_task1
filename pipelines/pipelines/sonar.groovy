pipeline {
    agent {
        label "slave1"
    }

    stages {
      stage('GIT') {
          steps {
            git 'https://github.com/fhause5/petclinic-be.git'
          }
      }
      stage('Sonarqube') {
        steps {
          withSonarQubeEnv(credentialsId: 'backend', installationName: 'sonar_scanner') {
            sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.6.0.1398:sonar'
          }
        }
      }
    }
}
