/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/configuration',
    'jquery'
], function(configuration, $) {
    'use strict';

    function hackUrl(url) {
        // TODO: remove this before any proper deployment
        // We have to map sites like
        //   http://demosharepoint/sites/InfoCenter/CS/Manuals_EN/...
        // to
        //   https://sharepoint.rowini.net/dvsz-sites/PBSupport-Wiki/Manuals%20(EN)/...

        return url && $.trim(url).replace(/http:\/\/demosharepoint\/sites\/InfoCenter\/CS\/([^/]+)_EN/i, 'https://sharepoint.rowini.net/dvsz-sites/PBSupport-Wiki/$1%20(EN)');
    }

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

            return hackUrl(prefix + url);
        },

        hackUrl: hackUrl,

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
