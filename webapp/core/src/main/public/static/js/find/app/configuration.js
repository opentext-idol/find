/*
 * Copyright 2016-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'underscore'
], function($, _) {
    'use strict';

    var config;

    var parseBooleanOption = function(config, uiCustomization, option) {
        var optionRules = uiCustomization.options[option];

        return optionRules.user && !(config.hasBiRole && optionRules.bi === false) ||
            optionRules.bi && config.hasBiRole;
    };

    return function() {
        if(!config) {
            var configString = $('#config-json').text();

            if(configString) {
                config = JSON.parse(configString);
            }

            if(!config.hasBiRole) {
                config.hasBiRole = _.contains(config.roles, 'ROLE_BI');
            }

            var uiCustomization = config.uiCustomization;

            if(uiCustomization) {
                config.directAccessLink = parseBooleanOption(config, uiCustomization, 'directAccessLink');
                config.enableMetaFilter = parseBooleanOption(config, uiCustomization, 'enableMetaFilter');
                config.enableRelatedConcepts = parseBooleanOption(config, uiCustomization, 'enableRelatedConcepts');
                config.enableTypeAhead = parseBooleanOption(config, uiCustomization, 'enableTypeAhead');
                config.errorCallSupportString = uiCustomization.errorCallSupportString;

                config.resultViewOrder = config.hasBiRole &&
                    uiCustomization.options.resultViewOrder.bi ||
                    uiCustomization.options.resultViewOrder.user;
            }
        }
        return config;
    }
});
