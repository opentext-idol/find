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
