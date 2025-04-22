pipeline {
  agent any

  environment {
    DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials') 
  }

  stages {
    stage('Generate Tag') {
      steps {
        script {
          def timestamp = new Date().format("yyyyMMddHHmmss", TimeZone.getTimeZone('UTC'))
          env.IMAGE_TAG = timestamp
          echo "Generated Image Tag: ${env.IMAGE_TAG}"
        }
      }
    }

    stage('Build Backend') {
      steps {
        dir('backend') {
          script {
            docker.build("tigerwk/ai-backend:${env.IMAGE_TAG}", '.')
          }
        }
      }
    }

    stage('Build Frontend') {
      steps {
        dir('frontend') {
          script {
            docker.build("tigerwk/ai-frontend:${env.IMAGE_TAG}", '.')
          }
        }
      }
    }

    stage('Push Docker Images') {
      steps {
        script {
          docker.withRegistry('https://index.docker.io/v1/', DOCKERHUB_CREDENTIALS) {
            docker.image("tigerwk/ai-backend:${env.IMAGE_TAG}").push()
            docker.image("tigerwk/ai-frontend:${env.IMAGE_TAG}").push()
          }
        }
      }
    }
  }
}
