/*
 * Copyright 2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
