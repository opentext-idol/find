/*
 * Copyright 2016-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery'
], function(_, $) {
    'use strict';

    let config;

    function parseBooleanOption(config, uiCustomization, option) {
        const optionRules = uiCustomization.options[option];

        return optionRules.user && !(config.hasBiRole && optionRules.bi === false) ||
            optionRules.bi && config.hasBiRole;
    }

    return function() {
        if(!config) {
            const configString = $('#config-json').text();

            if(configString) {
                config = JSON.parse(configString);

                if(!config.hasBiRole) {
                    config.hasBiRole = _.contains(config.roles, 'ROLE_BI');
                }

                const uiCustomization = config.uiCustomization;

                if(uiCustomization) {
                    config.enableDashboards = parseBooleanOption(config, uiCustomization, 'enableDashboards') && !_.isEmpty(config.dashboards);
                    config.enableMetaFilter = parseBooleanOption(config, uiCustomization, 'enableMetaFilter');
                    config.enableRelatedConcepts = parseBooleanOption(config, uiCustomization, 'enableRelatedConcepts');
                    config.enableSavedSearch = parseBooleanOption(config, uiCustomization, 'enableSavedSearch');
                    config.enableSideBar = parseBooleanOption(config, uiCustomization, 'enableSideBar') && (config.enableDashboards || !_.isEmpty(config.applications));
                    config.enableTypeAhead = parseBooleanOption(config, uiCustomization, 'enableTypeAhead');
                    config.errorCallSupportString = uiCustomization.errorCallSupportString;

                    config.resultViewOrder = config.hasBiRole &&
                        uiCustomization.options.resultViewOrder.bi ||
                        uiCustomization.options.resultViewOrder.user;
                }
            }
            else {
                config = {};
            }
        }

        return config;
    }
});
