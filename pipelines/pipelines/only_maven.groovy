def label = "mypod-${UUID.randomUUID().toString()}"
podTemplate(label: label, containers: [
    containerTemplate(name: 'maven',
    image: 'maven:3.3.9-jdk-8-alpine',
    ttyEnabled: true,
    command: 'cat',
    resourceReaquestCpu: '50m',
    resourceLimitCpu: '100m',
    resourceRequestMemory: '100Mi',)
  ]) {


    node(label) {
        stage('Get a maven project') {
            git 'https://github.com/fhause5/petclinic-be.git'
            container('maven') {
                stage('Build a maven project') {
                    sh 'mvn -DskipTests -B clean install'
                }
                stage('Test') {
                    sh 'mvn -version'
            }
        }
    }
}
