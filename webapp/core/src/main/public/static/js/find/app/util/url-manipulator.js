/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/configuration'
], function(configuration) {
    'use strict';

    return {
        addSpecialUrlPrefix: function(contentType, url) {
            //noinspection JSUnresolvedVariable
            const uiCustomization = configuration().uiCustomization;

            let prefix = '';
            //noinspection JSUnresolvedVariable
            if(uiCustomization && uiCustomization.specialUrlPrefixes && contentType && uiCustomization.specialUrlPrefixes[contentType]) {
                //noinspection JSUnresolvedVariable
                prefix = uiCustomization.specialUrlPrefixes[contentType];
            }

            return prefix + url;
        },

        appendHashFragment: function(model, url) {
            const hashFragmentIndex = model.get('reference').indexOf('#');
            if (hashFragmentIndex === -1) {
                return url;
            } else {
                return url + model.get('reference').substring(hashFragmentIndex);
            }
        }
    };
});
