pipeline {
    agent any

    triggers {
        // Automatically trigger this pipeline on GitHub push events
        githubPush()
    }

    stages {
        stage('Build') {
            steps {
                echo 'called from git'
                sh 'date'
                sh 'git rev-parse HEAD || true'
            }
        }

        stage('Post-build check') {
            steps {
                echo 'Pipeline finished successfully'
            }
        }
    }
}
