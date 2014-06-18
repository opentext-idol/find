# -*- mode: ruby -*-
# vi: set ft=ruby :

name 'base-role'
description 'Base role that sets up common services etc'
run_list 'recipe[apt]', 'recipe[build-essential]', 'recipe[vim]', 'recipe[frontend-common-cookbook::basic-tools]'
