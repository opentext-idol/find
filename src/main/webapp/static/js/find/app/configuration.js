define([
    'jquery'
], function() {

    var config;

    return function() {
        if (!config) {
            var configString = $('#config-json').text();

            if (configString) {
                config = JSON.parse(configString);
            }
        }

        return config;
    }

})