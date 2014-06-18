#!/bin/bash

if [ ! -f /opt/provisioning/updatechef ]; then

	gem uninstall chef
	gem install chef --no-ri --no-rdoc --version '11.6.2'

	mkdir /opt/provisioning
	touch /opt/provisioning/updatechef
fi