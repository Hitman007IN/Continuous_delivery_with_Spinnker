//def GOOGLE_APPLICATION_CREDENTIALS = '/home/jenkins/dev/jenkins-deploy-dev-infra.json'

podTemplate(
    label: 'mypod', 
    inheritFrom: 'default',
    containers: [
        containerTemplate(
            name: 'maven', 
            image: 'maven:3.6.3-jdk-11-openj9',
            ttyEnabled: true,
            command: 'cat'
        ),
        containerTemplate(
            name: 'docker', 
            image: 'docker:18.02',
            ttyEnabled: true,
            command: 'cat'
        )
    ],
    volumes: [
        hostPathVolume(
            hostPath: '/var/run/docker.sock',
            mountPath: '/var/run/docker.sock'
        )
    ]
) {

    node('mypod') {
        
        def PROJECT = "flawless-mason-258102"
        def CLUSTER = "spinnaker-cd"
        def CLUSTER_ZONE = "us-east1-d"
        def JENKINS_CRED = "${PROJECT}"
        def APP_SERVICE1 = "service_new"
        def TAG_ID = "1.0.0"
        def commitId

        stage ('Git Checkout') {
            checkout scm
            commitId = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
        }
        stage ('Maven Build and Test') {
            container ('maven') {
                sh '''cd serviceA
                mvn clean install'''
            }
        }
        def repository
        stage ('Docker Build and Push') {

            container ('docker') {
                def registryIp = "gcr.io/flawless-mason-258102"

                sh "cat keyfile.json | docker login -u _json_key --password-stdin https://gcr.io"

                sh "docker build -t service_new:1.0.0 serviceA/."
                sh "docker tag service_new:1.0.0 ${registryIp}/service_new:1.0.0"
                sh "docker push ${registryIp}/service_new:1.0.0"

            }
        }
        
    }
}

