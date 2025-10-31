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
            set -e
            mkdir -p "$HOME/.m2"

            if [ -f "${WORKSPACE}/jenkins.d/settings-template.xml" ]; then
                cp "${WORKSPACE}/jenkins.d/settings-template.xml" "$HOME/.m2/settings.xml"
            elif [ -f "/jenkins.d/settings-template.xml" ]; then
                cp /jenkins.d/settings-template.xml "$HOME/.m2/settings.xml"
            else
                echo "ERROR: settings-template.xml not found" >&2
                exit 1
            fi

            sed -i "s|__BUILD_USER__|${NEXUS_BUILD_USER}|g" "$HOME/.m2/settings.xml"
            sed -i "s|__BUILD_PASSWORD__|${NEXUS_BUILD_PASS}|g" "$HOME/.m2/settings.xml"
            sed -i "s|__DEPLOY_USER__|${NEXUS_DEPLOY_USER}|g" "$HOME/.m2/settings.xml"
            sed -i "s|__DEPLOY_PASSWORD__|${NEXUS_DEPLOY_PASS}|g" "$HOME/.m2/settings.xml"

            echo '--- Verify resulting file ---'
            ls -ld "$HOME/.m2/settings.xml"
            grep -A3 "<server>" "$HOME/.m2/settings.xml" || true
        '''
    }
}

return this