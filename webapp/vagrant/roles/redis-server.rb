# -*- mode: ruby -*-
# vi: set ft=ruby :

name 'redis-server'
description 'Redis datastore server'
run_list 'recipe[redis]', 'recipe[redis::server]'

override_attributes 'redis' => {
  :bind => '0.0.0.0'
}
