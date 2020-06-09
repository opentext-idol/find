/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
