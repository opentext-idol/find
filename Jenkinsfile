#!groovy

import groovy.transform.Field

@Field
def gitCommit
@Field
def repository
@Field
def branch

properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '3', daysToKeepStr: '', numToKeepStr: ''))])

node {
	stage 'Checkout'
		checkout scm

		sh 'git clean -ffdx' // Clean workspace: ultra-force (ff), untracked directories as well as files (d), don't use .gitignore (x)

		gitCommit = getGitCommit()
		repository = getOrgRepoName().toLowerCase()
		branch = "${env.BRANCH_NAME}"

		echo "Building ${gitCommit}, from ${repository}, branch ${branch}"

		def webapp = "find"

	stage 'Maven Build'
		env.JAVA_HOME="${tool 'Java 8 OpenJDK'}"
		env.PATH="${tool 'Maven3'}/bin:${env.JAVA_HOME}/bin:${env.PATH}"

		try {
			sh "mvn clean install -f webapp/pom.xml -U -Pproduction -pl on-prem-dist,selenium-tests/mockui -am -Dapplication.buildNumber=${gitCommit} -Dtest.community.host=cbg-data-admin-dev.hpeswlab.net -Dtest.content.host=cbg-data-admin-dev.hpeswlab.net -Dtest.view.host=cbg-data-admin-dev.hpeswlab.net -Dtest.answer.host=cbg-data-admin-dev.hpeswlab.net -Dtest.database=GenericDocuments"
		} catch (e) {
			emailext attachLog: true, body: "Check console output at ${env.BUILD_URL} to view the results.", subject: "Fenkins - ${env.JOB_NAME} - Build # ${env.BUILD_NUMBER} - ${currentBuild.result}", to: '$DEFAULT_RECIPIENTS'
			throw e
		}

	stage 'Archive output'
		archive 'idol/target/${webapp}.war'
		archive 'on-prem-dist/target/${webapp}.zip'

		// These are the JUnit tests as outputted by the surefire maven plugin
		step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])

		// These are the Jasmine tests
		step([$class: 'JUnitResultArchiver', testResults: '**/target/jasmine-tests/TEST-*.xml'])

	stage 'Artifactory'
		try {
			def server = Artifactory.server "idol" // "idol" is the name of the Artifactory server configured in Jenkins
			def artifactLocation = "applications/${repository}/${branch}/"

			def uploadSpec = """{
				"files": [
					{
						"pattern": "webapp/idol/target/*.war",
						"target": "${artifactLocation}"
					},
					{
						"pattern": "webapp/on-prem-dist/target/*.zip",
						"target": "${artifactLocation}"
					}
				]
			}"""

			withEnv(["GIT_COMMIT=${gitCommit}"]) {
				def buildInfo = Artifactory.newBuildInfo()
				buildInfo.env.capture = true
				buildInfo.env.collect()

				server.upload(uploadSpec, buildInfo)
			}
		} catch (org.acegisecurity.acls.NotFoundException e) {
			echo "No Artifactory 'idol' server configured, skipping stage"
		} catch (groovy.lang.MissingPropertyException e) {
		    echo "No Artifactory plugin installed, skipping stage"
		}

    stage 'Deploy'
        echo "webapp = ${webapp}"
        echo "repository_location = ${repository}"
        echo "branch = ${branch}"

        sh """
            config_template_name=onprem-config.json.j2
            config_template_location=\$(realpath webapp/hsod-dist/src/ansible/${webapp}/templates/\${config_template_name})
            ANSIBLE_HOST_KEY_CHECKING=False ANSIBLE_ROLES_PATH=\${FRONTEND_PLAYBOOK_PATH}/roles ansible-playbook \${FRONTEND_PLAYBOOK_PATH}/playbooks/app-playbook.yml -vv -i \${FRONTEND_PLAYBOOK_PATH}/hosts --become-user=fenkins --extra-vars "webapp=${webapp} docker_compose_src=find-docker-compose.yml repository_location=${repository} branch=${branch} docker_build_location=/home/fenkins/docker_build config_template_location=\${config_template_location} config_template_name=\${config_template_name}"
        """
}

def getGitCommit() {
	sh (
		script: "git rev-parse --short HEAD",
		returnStdout: true
	).trim()
}

/**
* Perl regex for converting the first line of the remote information into a directory path.
* Matches the text contained within either of the captures "git@" or https?:// at the start
* and captures (fetch) or (push) with removed .git if present at the end.
*/
def getOrgRepoName() {
	sh (
		script: "git remote -v | head -1 | perl -pe 's~^.*?(?:git@|https?://)([^:/]*?)[:/](.*?)(?:\\.git)?\\s*\\((?:fetch|push)\\)\$~\\1/\\2~p'",
		returnStdout: true
	).trim()
}
