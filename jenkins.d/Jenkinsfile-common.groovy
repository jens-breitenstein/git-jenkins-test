def buildAndDeploy(String dirName, String jdkLabel) {
    pipeline {
        agent { label jdkLabel }

        triggers {
            githubPush()
        }

        stages {
            stage('Prepare Maven settings') {
                steps {
                    script {
                        echo "Running in Jenkins CI — using secure template."
                        withCredentials([
                            usernamePassword(
                                credentialsId: 'nexus-build',
                                usernameVariable: 'NEXUS_BUILD_USER',
                                passwordVariable: 'NEXUS_BUILD_PASS'
                            ),
                            usernamePassword(
                                credentialsId: 'nexus-deploy',
                                usernameVariable: 'NEXUS_DEPLOY_USER',
                                passwordVariable: 'NEXUS_DEPLOY_PASS'
                            )
                        ]) {
                            sh '''
                                mkdir -p ~/.m2
                
                                # Copy from workspace (primary source)
                                if [ -f "${WORKSPACE}/jenkins.d/settings-template.xml" ]; then
                                    cp "${WORKSPACE}/jenkins.d/settings-template.xml" ~/.m2/settings.xml
                                elif [ -f "/jenkins.d/settings-template.xml" ]; then
                                    # fallback if mounted separately (legacy setup)
                                    cp /jenkins.d/settings-template.xml ~/.m2/settings.xml
                                else
                                    echo "ERROR: Could not find settings-template.xml" >&2
                                    exit 1
                                fi
                
                                # Replace placeholders with secured credentials
                                sed -i "s|__BUILD_USER__|${NEXUS_BUILD_USER}|g" ~/.m2/settings.xml
                                sed -i "s|__BUILD_PASSWORD__|${NEXUS_BUILD_PASS}|g" ~/.m2/settings.xml
                                sed -i "s|__DEPLOY_USER__|${NEXUS_DEPLOY_USER}|g" ~/.m2/settings.xml
                                sed -i "s|__DEPLOY_PASSWORD__|${NEXUS_DEPLOY_PASS}|g" ~/.m2/settings.xml
                            '''
                        }
                    }
                }
            }

            stage('Build') {
                steps {
                    dir(dirName) {
                        echo "=== Building ${dirName} with ${jdkLabel} ==="
                        sh "mvn clean install -DskipTests"
                    }
                }
            }

            stage('Deploy to Nexus') {
                when {
                    expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
                }
                steps {
                    dir(dirName) {
                        echo "=== Deploying ${dirName} with ${jdkLabel} ==="
                        sh "mvn deploy -DskipTests"
                    }
                }
            }
        }

        post {
            success { echo "Build & Deploy for ${dirName} successful." }
            failure { echo "Build for ${dirName} failed." }
            always  { cleanWs() }
        }
    }
}
