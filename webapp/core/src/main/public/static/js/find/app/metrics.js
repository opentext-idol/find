/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'find/app/configuration'
], function($, configuration) {
    'use strict';

    function addMetric(name, timeInMillis) {
        if(enabled()) {
            $.ajax('api/public/metrics/add', {
                data: {
                    metricName: name,
                    timeInMillis: timeInMillis
                },
                method: 'POST'
            });
        }
    }

    function enabled() {
        return configuration().metricsEnabled;
    }

    return {
        addMetric: addMetric,
        addTimeSincePageLoad: function(name) {
            addMetric(name, performance.now());
        },
        enabled: enabled
    };
});
