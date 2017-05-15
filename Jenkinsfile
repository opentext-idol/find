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
		branch = getBranchName(gitCommit).toLowerCase()

		def webapp = "find"

    stage 'Deploy'
        echo "webapp = ${webapp}"
        echo "repository_location = ${repository}"
        echo "branch = ${branch}"

        sh """
            FPLAYBOOKDIR=/home/fenkins/frontend-playbook/vagrant/ansible/frontendslave-playbook/
            config_template_name=onprem-config.json.j2
            config_template_location=\$(realpath webapp/hsod-dist/src/ansible/${webapp}/templates/\${config_template_name})
            ANSIBLE_HOST_KEY_CHECKING=False ANSIBLE_ROLES_PATH=\${FPLAYBOOKDIR}roles ansible-playbook \${FPLAYBOOKDIR}playbooks/app-playbook.yml -vv -i \${FPLAYBOOKDIR}hosts --become-user=fenkins --extra-vars "webapp=${webapp} repository_location=${repository} branch=${branch} docker_build_location=/home/fenkins/docker_build config_template_location=\${config_template_location} config_template_name=\${config_template_name}"
        """
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
