define([
    'jquery',
    'find/app/configuration'
], function($, configuration) {
    "use strict";

    function addMetric(name, timeInMillis) {
        if (configuration().metricsEnabled) {
            $.ajax('api/public/metrics/add', {
                data: {
                    metricName: name,
                    timeInMillis: timeInMillis
                },
                method: 'POST'
            });
        }
    }

    return {
        addMetric: addMetric,
        addTimeSincePageLoad: function (name) {
            addMetric(name,  performance.now());
        }
    };
});