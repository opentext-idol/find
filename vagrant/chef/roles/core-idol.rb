# -*- mode: ruby -*-
# vi: set ft=ruby :

name 'core-idol'
description 'Core IDOL Components'
run_list 'frontend-common-cookbook::aci-deploy'

override_attributes 'frontend-common' => {
    'aci-deploy' => {
        'components' => [{
            'name' => 'licenseserver',
            'component' => 'licenseserver',
            'ports' => {
                'aci' => 20000,
                'service' => 20002
            },
            'licenseKey' => 'core/licensekey.dat',
            'configFile' => 'core/licenseserver.cfg',
            'version' => 'latest',
            'deployIdolAdmin' => false,
            'priority' => 78
        }, {
            'name' => 'agentstore',
            'component' => 'agentstore',
            'ports' => {
                'aci' => 9050,
                'index' => 9051,
                'service' => 9052
            },
            'configFile' => "core/agentstore.cfg",
            'version' => 'latest',
            'deployIdolAdmin' => false,
            'priority' => 79
        }, {
            'name' => 'community',
            'component' => 'community',
            'ports' => {
                'aci' => 9030,
                'service' => 9032
            },
            'configFile' => "core/community.cfg",
            'version' => 'latest',
            'deployIdolAdmin' => false,
            'priority' => 80
        }]
    }
}
