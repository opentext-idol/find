# -*- mode: ruby -*-
# vi: set ft=ruby :

name "tomcat7-server"
description "Tomcat 7 Java Web Application server"
run_list "recipe[tomcat]"

override_attributes "tomcat" => {
		"base_version" => 7,
		"keytool" => "/usr/bin/keytool"
	},
	"java" => {
		"jdk_version" => "7"
	}
