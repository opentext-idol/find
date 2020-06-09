/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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
    'js-whatever/js/location'
], function(_, location) {
    'use strict';

    function supported(configuration, attributes) {
        return configuration.mmapBaseUrl
            && attributes.mmapUrl
            && attributes.mmapEventSourceType
            && attributes.mmapEventSourceName
            && attributes.mmapEventTime;
    }

    const baseUrlRegex = /(?:\w+:\/\/)?([^:/]+(?::\d+)?)(?:\/[^/]+)*/;

    function canBeReused(configuration) {
        return configuration.mmapBaseUrl
            && location.host()
            && baseUrlRegex.exec(configuration.mmapBaseUrl)[1] === baseUrlRegex.exec(location.host())[1];
    }

    return function(configuration) {
        let childWindow;

        return {
            supported: _.partial(supported, configuration),
            canBeReused: _.partial(canBeReused, configuration),
            open: function(attributes) {
                if(supported(configuration, attributes)) {
                    if(canBeReused(configuration) && childWindow && !childWindow.closed) {
                        const sourceType = attributes.mmapEventSourceType;
                        const sourceName = attributes.mmapEventSourceName;
                        const startTime = attributes.mmapEventTime;
                        if(sourceType === 'Camera') {
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
