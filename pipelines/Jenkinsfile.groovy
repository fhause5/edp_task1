pipeline {
    agent{
        node{
            label 'slave1'
        }
    }
            tools {
                maven 'maven'
                jdk 'jdk'
            } 
    stages {
        stage ('Git Checkout') {
            steps{
           git 'https://github.com/DanyMady/petclinic-be'
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
        
              withSonarQubeEnv('sonarQube') {
                sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.6.0.1398:sonar'
        }
        timeout(time: 10, unit: 'MINUTES') {
            waitForQualityGate abortPipeline: true
        }
    }
}
      stage('Building image') {
//                     environment {
//             registry = "danymady1/back-end"
//             registryCredential = 'dockerhub'
//   }
      steps{
        script {
            sh 'pwd'
            sh 'ls'
            // sh 'docker build -f "Dockerfile" -t danymady1/back-end .'a
            // sh 'docker push danymady1/back-end'
            dockerImage = docker.build("danymady1/back-end")
            docker.withRegistry('https://registry-1.docker.io/v2/','docker-hub') {
               dockerImage.push()
    }
        }
      }  
        }

          stage ('Initialising kubectl with jenkins') {
            steps {
            sh 'curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.15.0/bin/linux/amd64/kubectl'
            sh 'chmod +x ./kubectl'
            sh 'sudo mv ./kubectl /usr/local/bin/kubectl'
            sh 'sudo cat ~/.kube/config'
        }
    }
            

          stage ('Deploy') {
            steps {
                withKubeConfig('KubeConfig') {
                sh 'kubectl apply -f /deployment/* -n petclinic'
                // sh 'cd backend'
                // sh './mvnw spring-boot:run -P hsqldb'
        }
    }
            }
        }
}