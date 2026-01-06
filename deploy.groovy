pipeline {
    agent {
        docker {
            image 'ubuntu:22.04'
            args '-u root:root'
        }
    }

    environment {
        SSH_KEY64 = credentials('SSH_KEY64') // Your Jenkins stored SSH key in base64
    }

    parameters {
        string(
            name: 'SERVER_IP',
            defaultValue: '44.223.7.233',
            description: 'Target EC2 Server IP'
        )
    }

    stages {

        stage('Install Tools') {
            steps {
                sh '''
                apt-get update
                apt-get install -y git rsync openssh-client
                '''
            }
        }

        stage('Checkout Code from GitHub') {
            steps {
                sh '''
                rm -rf repo
                git clone -b main https://github.com/arugnata/try-jenkins.git repo
                '''
            }
        }

        stage('Setup SSH Key') {
            steps {
                sh '''
                # Decode Jenkins stored SSH key
                echo "$SSH_KEY64" | base64 -d > mykey.pem
                chmod 400 mykey.pem

                # Remove existing entry if host exists
                ssh-keygen -R ${SERVER_IP} || true
                '''
            }
        }

        stage('Deploy Code to Server') {
            steps {
                sh '''
                # Deploy repo using rsync with correct SSH command
                rsync -avz --delete -e "ssh -i mykey.pem -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null" repo/ ubuntu@${SERVER_IP}:/var/www/html/
                '''
            }
        }
    }
}

