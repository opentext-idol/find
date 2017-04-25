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
		repository = getOrgRepoName()
		branch = getBranchName(gitCommit)

		echo "Building ${gitCommit}, from ${repository}, branch ${branch}"

	stage 'Maven Build'
		env.JAVA_HOME="${tool 'Java 8 OpenJDK'}"
		env.PATH="${tool 'Maven3'}/bin:${env.JAVA_HOME}/bin:${env.PATH}"

		sh "mvn clean install -f webapp/pom.xml -U -pl on-prem-dist,selenium-tests/mockui -am -Dapplication.buildNumber=${gitCommit} -Dtest.content.host=cbg-data-admin-dev.hpeswlab.net -Dtest.view.host=cbg-data-admin-dev.hpeswlab.net -Dtest.answer.host=cbg-data-admin-dev.hpeswlab.net -Dtest.database=GenericDocuments"

	stage 'Archive output'
		archive 'idol/target/find.war'
		archive 'on-prem-dist/target/find.zip'

		// These are the JUnit tests as outputted by the surefire maven plugin
		step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])

		// These are the Jasmine tests
		step([$class: 'JUnitResultArchiver', testResults: '**/target/jasmine-tests/TEST-*.xml'])

	stage 'Artifactory'
		try {
			def server = Artifactory.server "idol" // "idol" is the name of the Artifactory server configured in Jenkins
			def artifactLocation = "applications/find/${repository}/${branch}/"

			def uploadSpec = """{
				"files": [
					{
						"pattern": "webapp/idol/target/*.war",
						"target": "${artifactLocation};commit=${gitCommit}"
					},
					{
						"pattern": "webapp/hod/target/*.war",
						"target": "${artifactLocation}"
					},
					{
						"pattern": "webapp/on-prem-dist/target/*.zip",
						"target": "${artifactLocation}"
					},
					{
						"pattern": "webapp/hsod-dist/target/*.zip",
						"target": "${artifactLocation}"
					}
				]
			}"""

			server.upload(uploadSpec)
		} catch (org.acegisecurity.acls.NotFoundException e) {
			echo "No Artifactory 'idol' server configured, skipping stage"
		} catch (groovy.lang.MissingPropertyException e) {
		    echo "No Artifactory plugin installed, skipping stage"
		}

	stage 'Notifications'
		emailext attachLog: true, body: "Check console output at ${env.BUILD_URL} to view the results.", subject: "Fenkins - ${env.JOB_NAME} - Build # ${env.BUILD_NUMBER} - ${currentBuild.result}", to: '$DEFAULT_RECIPIENTS'
}

def getGitCommit() {
	sh (
		script: "git rev-parse --short HEAD",
		returnStdout: true
	).trim()
}

/**
* Looks up branch of the current commit on the remote to determine the branch being built.
*/
def getBranchName(gitCommit) {
	sh (
		script: "git branch --remote --contains ${gitCommit}",
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
