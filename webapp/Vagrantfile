# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

vm_name = 'hp-find-backend'

required_plugins = %w(vagrant-hostsupdater vagrant-librarian-chef vagrant-proxyconf)

required_plugins.each do |plugin|
  system "vagrant plugin install #{plugin}" unless Vagrant.has_plugin? plugin
end

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  # All Vagrant configuration is done here. The most common configuration
  # options are documented and commented below. For a complete reference,
  # please see the online documentation at vagrantup.com.

  # Every Vagrant virtual environment requires a box to build off of.
  config.vm.box = "hashicorp/precise64"

  config.vm.network "private_network", ip: '192.168.242.242'

  config.vm.hostname = vm_name

  config.vm.provider :virtualbox do |vb|
    # Don't boot with headless mode
    # Uncomment this if you get stuck at the bootloader
    # vb.gui = true

    vb.name = vm_name
  end

  config.vm.provider 'vmware_workstation' do |hv|
    hv.name = vm_name
  end

  config.proxy.http = ENV["http_proxy"]
  config.proxy.https = ENV["https_proxy"]
  config.proxy.no_proxy = ENV["no_proxy"]

  # Enable provisioning with chef solo, specifying a cookbooks path, roles
  # path, and data_bags path (all relative to this Vagrantfile), and adding
  # some recipes and/or roles.
  #
  config.vm.provision :chef_solo do |chef|
    chef.cookbooks_path = "cookbooks"
    chef.roles_path = "vagrant/roles"
    chef.add_role "redis-server"
  end
end
