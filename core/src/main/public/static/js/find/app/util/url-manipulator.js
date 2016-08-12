/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/configuration'
], function(configuration) {
    "use strict";
    
    return {
        addSpecialUrlPrefix: function (contentType, url) {
            //noinspection JSUnresolvedVariable
            const uiCustomization = configuration().uiCustomization;

            let prefix = '';
            //noinspection JSUnresolvedVariable
            if (uiCustomization && uiCustomization.specialUrlPrefixes && contentType && uiCustomization.specialUrlPrefixes[contentType]) {
                //noinspection JSUnresolvedVariable
                prefix = uiCustomization.specialUrlPrefixes[contentType];
            }
            
            return prefix + url;
        }
    };
});