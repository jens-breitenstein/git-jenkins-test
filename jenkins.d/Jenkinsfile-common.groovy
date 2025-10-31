def setupMavenSettings() {
    echo "Running in Jenkins CI — using secure template."
    withCredentials([
        usernamePassword(
            credentialsId: 'jenkins-build',
            usernameVariable: 'NEXUS_BUILD_USER',
            passwordVariable: 'NEXUS_BUILD_PASS'
        ),
        usernamePassword(
            credentialsId: 'jenkins-deploy',
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

def buildAndDeploy(String dirName, String jdkLabel) {
    stage("Prepare Maven settings") {
        setupMavenSettings()
    }

    stage("Build") {
        dir(dirName) {
            echo "=== Building ${dirName} with ${jdkLabel} ==="
            sh "mvn clean install"
        }
    }

    stage("Deploy to Nexus") {
        if (currentBuild.resultIsBetterOrEqualTo('SUCCESS')) {
            dir(dirName) {
                echo "=== Deploying ${dirName} with ${jdkLabel} ==="
                sh "mvn deploy -DskipTests"
            }
        }
    }
}
return this
