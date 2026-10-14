// CI/CD pipeline: runs automatically on every push to GitHub.
// (On Windows Jenkins, replace every `sh` with `bat`.)
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps { checkout scm }                 // pull latest code from GitHub
        }
        stage('Build') {
            steps { bat 'mvn clean compile' }
        }
        stage('Test') {                            // Spring Boot tests + Karate tests
            steps { bat 'mvn test' }
        }
        stage('Docker Build') {
            steps { bat 'docker build -t blood-bank:latest .' }
        }
        stage('Deploy to Kubernetes') {
            steps {
                bat 'kubectl apply -f k8s.yaml'
                bat 'kubectl rollout restart deployment blood-bank'   // pick up the new image
            }
        }
    }

    post {
        always  { junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml' }   // show test results in Jenkins
        success { echo 'Pipeline passed - app deployed!' }
        failure { echo 'Pipeline failed - check the logs.' }
    }
}
