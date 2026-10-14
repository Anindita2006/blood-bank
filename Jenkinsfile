pipeline {
    agent any

    tools {
        maven 'maven'
    }

    environment {
        DOCKER      = 'C:\\Users\\anind\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe'
        KUBECTL     = 'C:\\Users\\anind\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\kubectl.exe'
        DOCKER_HOST = 'tcp://localhost:2375'
        KUBECONFIG  = 'C:\\Users\\anind\\.kube\\config'
    }

    stages {
        stage('Checkout')     { steps { checkout scm } }
        stage('Build')        { steps { bat 'mvn clean compile' } }
        stage('Test')         { steps { bat 'mvn test' } }
        stage('Docker Build') { steps { bat '"%DOCKER%" build -t blood-bank:latest .' } }
        stage('Deploy to Kubernetes') {
            steps {
                bat '"%KUBECTL%" apply -f k8s.yaml'
                bat '"%KUBECTL%" rollout restart deployment blood-bank'
            }
        }
    }

    post {
        always  { junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml' }
        success { echo 'Pipeline passed - app deployed!' }
        failure { echo 'Pipeline failed - check the logs.' }
    }
}