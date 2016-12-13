define([
    'underscore',
    'js-whatever/js/location'
], function (_, location) {
    "use strict";

    function supported (configuration, attributes) {
        return configuration.mmapBaseUrl
            && attributes.mmapUrl
            && attributes.mmapEventSourceType
            && attributes.mmapEventSourceName
            && attributes.mmapEventTime;
    }

    const baseUrlRegex = /(?:\w+:\/\/)?([^:/]+(?::\d+)?)(?:\/[^/]+)*/;

    function canBeReused (configuration) {
        return baseUrlRegex.exec(configuration.mmapBaseUrl)[1] === baseUrlRegex.exec(location.host())[1];
    }

    return function (configuration) {
        let childWindow;

        return {
            supported: _.partial(supported, configuration),
            canBeReused: _.partial(canBeReused, configuration),
            open: function (attributes) {
                if (supported(configuration, attributes)) {
                    if (canBeReused(configuration) && childWindow && !childWindow.closed) {
                        const sourceType = attributes.mmapEventSourceType;
                        const sourceName = attributes.mmapEventSourceName;
                        const startTime = attributes.mmapEventTime;
                        if (sourceType === 'Camera') {
                            //noinspection JSUnresolvedFunction
                            childWindow.loadCamera(sourceName, startTime);
                        } else {
                            //noinspection JSUnresolvedFunction
                            childWindow.loadChannel(sourceName, startTime);
                        }
                        childWindow.focus();
                    } else {
                        childWindow = window.open(configuration.mmapBaseUrl + attributes.mmapUrl);
                    }
                }
            }
        }
    }
});