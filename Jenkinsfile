pipeline {
    agent {
        kubernetes {
            // Change the name of jenkins-maven label to be able to use yaml configuration snippet
            label "maven-gke-preemtible"
            // Inherit from Jx Maven pod template
            inheritFrom "maven"
            // Add scheduling configuration to Jenkins builder pod template
            yamlFile "gke-preemptible.yaml"
        }
    }   
    environment {
      ORG               = 'introproventures'
      APP_NAME          = 'example-runtime-bundle'
      CHARTMUSEUM_CREDS = credentials('jenkins-x-chartmuseum')
    }
    stages {
      stage('CI Build and push snapshot') {
        when {
          branch 'PR-*'
        }
        environment {
          PREVIEW_VERSION = "0.0.0-SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER"
          PREVIEW_NAMESPACE = "$APP_NAME-$BRANCH_NAME".toLowerCase()
          HELM_RELEASE = "$PREVIEW_NAMESPACE".toLowerCase()
        }
        steps {
          container('maven') {
            sh "mvn versions:set -DnewVersion=$PREVIEW_VERSION"
            sh "mvn install -DskipTests"
            // sh 'export VERSION=$PREVIEW_VERSION && skaffold build -f skaffold.yaml'
            // sh "jx step post build --image $DOCKER_REGISTRY/$ORG/$APP_NAME:$PREVIEW_VERSION"
          }

          //dir ('./charts/preview') {
          // container('maven') {
          //   sh "make preview"
          //   sh "jx preview --app $APP_NAME --dir ../.."
          // }
          //}
        }
      }
      stage('Build Release') {
        when {
          branch 'develop'
        }
        steps {
          container('maven') {
            // ensure we're not on a detached head
            sh "git checkout develop"
            sh "git config --global credential.helper store"

            sh "jx step git credentials"
            // so we can retrieve the version in later steps
            sh "echo \$(jx-release-version) > VERSION"
            sh "mvn versions:set -DnewVersion=\$(cat VERSION)"
            sh 'mvn clean install -DskipTests'
          }
          dir ('./charts/example-runtime-bundle') {
            container('maven') {
              sh "make tag"
            }
          }
          container('maven') {
            sh 'mvn deploy -DskipTests'

            sh 'export VERSION=`cat VERSION` && skaffold build -f skaffold.yaml'

            sh "jx step post build --image $DOCKER_REGISTRY/$ORG/$APP_NAME:\$(cat VERSION)"
          }
        }
      }
      stage('Promote to Environments') {
        when {
          branch 'develop'
        }
        steps {
          dir ('./charts/example-runtime-bundle') {
            container('maven') {
              sh 'jx step changelog --version v\$(cat ../../VERSION)'

              // release the helm chart
              sh 'jx step helm release'

              // promote through all 'Auto' promotion Environments
              sh 'jx promote -b --all-auto --timeout 1h --version \$(cat ../../VERSION)'
            }
          }
        }
      }
    }
    post {
        always {
            cleanWs()
        }
    }
  }
