pipeline {

    environment {
        registry = "snap032/backend"
        registryCredential = 'dockerhub'
        dockerImage = ""
    }

    agent {
        node {
            label 'jenkins-jenkins-slave'
        }
    }

    stages {

        stage('Checkout Source') {
            steps {
                git 'https://github.com/fhause5/petclinic-be.git'
            }
        }

        stage('Build image') {
            steps{
                script {
                    dockerImage = docker.build registry + ":$BUILD_NUMBER"
                }
            }
        }

    stage('Deploy Image') {
      steps{
        script {
          docker.withRegistry( '', registryCredential ) {
            dockerImage.push()
          }
        }
      }
    }

        stage('Deploy App') {
            steps {
                script {
                    kubernetesDeploy(configs: "backend_postgress.yaml", kubeconfigId: "mykubeconfig")
                }
            }
        }

    }

}