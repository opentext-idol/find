# -*- mode: ruby -*-
# vi: set ft=ruby :

# Variables #


# Set the Project Name
projectname = 'find'

# Should we forward component ports from guest machines to the host?
shouldForwardPorts = false

# Should we deploy IDOL Component VMs?
deployIdolVMs = false

# Useful Vagrant plugins
Vagrant.require_plugin 'vagrant-cachier'			 # Cache stuff
Vagrant.require_plugin 'vagrant-proxyconf'		 # Configure HTTP proxies
Vagrant.require_plugin 'vagrant-hosts'				 # Add entries to the hosts files on the VMs
Vagrant.require_plugin 'vagrant-hostsupdater'	# Add VMs to the hosts file on your machine

require 'fileutils'

# Create directories for deploying IDOL components
Dir.mkdir('vagrant/packagecache') unless File.exists?('vagrant/packagecache')
Dir.mkdir('vagrant/scriptcache') unless File.exists?('vagrant/scriptcache')

# Load development site-specific profiles (e.g. proxies)
profiles = File.open("vagrant/profiles.json", "r") do |f|
  JSON.load(f)
end

# Pick the correct profile
profile = profiles[ENV['FRONTEND_VAGRANT_PROFILE'] || 'cambridge']

# Load list of components to install
components = File.open('vagrant/provisioning/idol-components.json', 'r') do |f|
  JSON.load(f)
end

# IP addresses
ip = {
  'backend' => '192.168.249.20'
}

# Work begins!
Vagrant.configure("2") do |config|

  # Use the frontent Vagrant Ubuntu 12.04 x64 box
  box_name = 'frontend-virtualbox-precise64-v1.0.2'
  config.vm.box_url = "http://idol-chef-packages.autonomy.com/vagrant/#box_name.box"
  config.vm.box = box_name

  # Configure vagrant-cachier plugin
  config.cache.auto_detect = true
  config.cache.scope = :machine

  # Configure vagrant-proxyconf plugin
  config.proxy.http       = profile["http_proxy"]
  config.proxy.https      = profile["https_proxy"]
  config.proxy.no_proxy   = profile["no_proxy"]

  # Sync script cache
  config.vm.synced_folder 'vagrant/scriptcache', '/tmp/scriptcache'

  # Sync IDOL package cache
  config.vm.synced_folder 'vagrant/packagecache', '/opt/deployment/cache'

  # Create a "Backend" VM with IDOL LicenseServer and PostgreSQL
  config.vm.define 'backend' do |node|
    node.vm.provision :hosts

    node.vm.provision :chef_solo do |chef|
      chef.roles_path = 'vagrant/chef/roles'
      chef.add_role('base-role')
      chef.add_role('core-idol')
      chef.add_role('postgres-server')
    end

    node.vm.hostname = "#{projectname}-backend"
    node.vm.network 'private_network', ip: ip['backend']

    if shouldForwardPorts
      forward_ports(node, [20000, 20002, 5432, 9030, 9050])
    end

    node.vm.provider :virtualbox do |vb|
      vb.name = "#{projectname}-backend"
    end
  end

  # GENERATE IDOL VMS #
  if deployIdolVMs
    components.each do |node|
      name = node['hostname']

      config.vm.define name do |machine|

        machine.vm.provision :hosts

        machine.vm.provision :chef_solo do |chef|
          chef.roles_path = 'vagrant/chef/roles'
          chef.add_role('base-role')

          chef.add_recipe 'frontend-common-cookbook::aci-deploy'
          chef.json = {
            'frontend-common' => {
              'aci-deploy' => {
                'components' => node['components']
              }
            }
          }
        end

        if shouldForwardPorts
          ports = []

          node['components'].each do |component|
            component['ports'].each do |type, port|
              ports.push(port)
            end
          end
          forward_ports(machine, ports)
        end

        machine.vm.hostname = "#{projectname}-#{name}"
        machine.vm.network 'private_network', ip: node['ip']

        machine.vm.provider :virtualbox do |vb|
          vb.name = machine.vm.hostname
        end
      end
    end
  end
end

def forward_ports(node, ports)
  ports.each do |port|
    node.vm.network :forwarded_port, guest: port, host: port, auto_correct: true
  end
end