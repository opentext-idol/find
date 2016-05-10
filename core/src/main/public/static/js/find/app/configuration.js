define([
    'jquery'
], function($) {

    var config;

    return function() {
        if (!config) {
            var configString = $('#config-json').text();

            if (configString) {
                config = JSON.parse(configString);
            }

            if (!config.hasBiRole) {
                config.hasBiRole = _.contains(config.roles, 'ROLE_BI');
            }
        }

        return config;
    }

});