define([
    'jquery',
    'underscore'
], function($, _) {
    "use strict";
    
    var config;

    var parseBooleanOption = function (config, uiCustomization, option) {
        var optionRules = uiCustomization.options[option];
        //noinspection JSUnresolvedVariable
        return optionRules.user && (!config.hasBiRole || optionRules.bi !== false) || optionRules.bi && config.hasBiRole;
    };

    return function() {
        if (!config) {
            var configString = $('#config-json').text();

            if (configString) {
                config = JSON.parse(configString);
            }

            if (!config.hasBiRole) {
                //noinspection JSUnresolvedFunction,JSUnresolvedVariable
                config.hasBiRole = _.contains(config.roles, 'ROLE_BI');
            }
            
            //noinspection JSUnresolvedVariable
            var uiCustomization = config.uiCustomization;
            if (uiCustomization) {
                config.directAccessLink = parseBooleanOption(config, uiCustomization, 'directAccessLink');
                config.enableMetaFilter = parseBooleanOption(config, uiCustomization, 'enableMetaFilter');
            }
        }

        return config;
    }

});