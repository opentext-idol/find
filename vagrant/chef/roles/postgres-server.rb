# -*- mode: ruby -*-
# vi: set ft=ruby :

name 'postgres-server'
description 'PostgreSQL database server'
run_list 'recipe[postgresql::server]', 'recipe[frontend-common-cookbook::apt-get-install]', 'recipe[frontend-common-cookbook::restart-services]'

override_attributes 'postgresql' => {
		'version' => '9.1',
		'password' => {
			'postgres' => 'md53175bce1d3201d16594cebf9d7eb3f9d' # Password is 'postgres'
		},
		'config' => {
			'listen_addresses' => '*',
			
		},
		'pg_hba' => [{
			:comment => '# local is for Unix domain socket connections only', :type => 'local', :db => 'all', :user => 'all', :addr => nil, :method => 'trust'
		}, {
			:comment => '# Allow all IPv4 connections', :type => 'host', :db => 'all', :user => 'all', :addr => '0.0.0.0/0', :method => 'md5'
		}]
	},
	'frontend-common' => {
		'apt-get-install' => %w(postgresql-contrib),
		'restart-services' => %w(postgresql)
	}
