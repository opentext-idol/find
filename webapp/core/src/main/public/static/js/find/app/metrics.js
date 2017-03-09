define([
    'jquery',
    'find/app/configuration'
], function($, configuration) {
    "use strict";

    function addMetric(name, timeInMillis) {
        if (enabled()) {
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
        addTimeSincePageLoad: function (name) {
            addMetric(name,  performance.now());
        },
        enabled: enabled
    };
});