pipeline {
    agent {
        docker {
            image 'ubuntu:22.04'
            args '-u root:root'
        }
    }

    environment {
        SSH_KEY64 = credentials('SSH_KEY64')
    }

    parameters {
        string(
            name: 'SERVER_IP',
            defaultValue: '44.223.7.233',
            description: 'Target Server'
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
                # Pull code from your new GitHub repo
                git clone -b main https://github.com/arugnata/try-jenkins.git repo
                '''
            }
        }

        stage('Configure SSH') {
            steps {
                sh '''
                mkdir -p ~/.ssh
                chmod 700 ~/.ssh

                echo -e "Host *\\n\\tStrictHostKeyChecking no\\n" > ~/.ssh/config
                chmod 600 ~/.ssh/config

                touch ~/.ssh/known_hosts
                chmod 600 ~/.ssh/known_hosts
                '''
            }
        }

        stage('SSH Key Access') {
            steps {
                sh '''
                echo "$SSH_KEY64" | base64 -d > mykey.pem
                chmod 400 mykey.pem

                ssh-keygen -R ${SERVER_IP} || true
                '''
            }
        }

        stage('Deploy Code to Server') {
            steps {
                sh '''
                # Sync GitHub code to EC2 /var/www/html
                rsync -avz --delete -e "ssh -i mykey.pem" repo/ ubuntu@${SERVER_IP}:/var/www/html/
                '''
            }
        }
    }
}
