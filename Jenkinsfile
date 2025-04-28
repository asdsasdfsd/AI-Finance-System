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

    stage('SAST - SonarQube Analysis') {
      steps {
        withSonarQubeEnv('sonarqube-server') {
          dir('backend') {
            sh './mvnw sonar:sonar -Dsonar.projectKey=ai-backend'
          }
          dir('frontend') {
            sh 'npm install -g sonar-scanner'
            sh 'sonar-scanner -Dsonar.projectKey=ai-frontend'
          }
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

    stage('DAST - OWASP ZAP Scan') {
      steps {
        script {         
          sh '''
            docker run --rm -v $(pwd):/zap/wrk/:rw -t owasp/zap2docker-stable zap-baseline.py \
            -t http://host.docker.internal:8080 -r zap_backend_report.html
          '''
          sh '''
            docker run --rm -v $(pwd):/zap/wrk/:rw -t owasp/zap2docker-stable zap-baseline.py \
            -t http://host.docker.internal:80 -r zap_frontend_report.html
          '''
        }
      }
    }
  }
}
