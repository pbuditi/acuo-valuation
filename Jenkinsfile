pipeline {

    agent { label 'ubuntu_agent' }
    triggers {
        pollSCM("")
    }
    

    stages {
        stage("checkout") {
            steps {
                    checkout scm
                    withCredentials([string(credentialsId: 'acuo_nexusPassword', variable: 'nexusPassword')]) {
                                   sh '''echo "nexusUrl=https://nexus.acuo.com" > gradle.properties
                        echo "nexusUsername=deployer" >> gradle.properties
                        echo "nexusPassword=$nexusPassword"  >> gradle.properties '''
                    }
                }
        }
        stage("Build") {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'acuo_aws_dev', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                        sh './gradlew snapshot dockerPushImage -Pprofile=docker  -x test -x integrationTest'
                }
            }  
        }
        stage("Deploy-Dev") {
            steps {
                sh 'ansible-galaxy install -r devops/ansible/requirements.yml -p devops/ansible/roles/'
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'acuo_aws_dev', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                    ansiblePlaybook credentialsId: '	pradeep-cloud-user', extras: "-e app_name=valuation -e app_branch=$BRANCH_NAME", inventory: 'devops/ansible/inventory/palo-dev', playbook: 'devops/ansible/playbook.yml', sudoUser: null
                }
            }  
        }
   }
    post {
        always {
            junit allowEmptyResults: true, testResults: 'valuation-app/build/test-results/test/*.xml'
        }
    }
}
