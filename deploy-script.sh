#!/usr/bin/env bash
source /home/fenkins/ansible/hacking/env-setup
FPLAYBOOKDIR=/home/fenkins/frontend-playbook/vagrant/ansible/frontendslave-playbook/
ANSIBLE_HOST_KEY_CHECKING=False ANSIBLE_ROLES_PATH=${FPLAYBOOKDIR}roles ansible-playbook ${FPLAYBOOKDIR}app-playbook.yml -vv -i ${FPLAYBOOKDIR}hosts --ask-sudo-pass --ask-vault-pass -k --extra-vars "docker_build_location=/home/fenkins/docker_build"