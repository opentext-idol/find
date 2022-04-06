/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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
    'js-whatever/js/vent-constructor',
    'find/app/configuration'
], function(_, VentConstructor, configuration) {
    'use strict';

    function CoreVent(router) {
        VentConstructor.call(this, router);
    }

    function stripLeadingSlash(string) {
        return string.indexOf('/') === 0 ? string.substring(1) : string;
    }

    CoreVent.prototype = Object.create(VentConstructor.prototype);

    _.extend(CoreVent.prototype, {
        constructor: CoreVent,

        navigateToDetailRoute: function(model) {
            this.navigate('search/document/' + this.addSuffixForDocument(model));
        },

        suggestRouteForDocument: function(model, databases) {
            let url = '/search/suggest/' + this.addSuffixForDocument(model);

            if (databases) {
                url += '/databases/' + databases;
            }

            return url;
        },

        suggestUrlForDocument: function(model, databases) {
            return stripLeadingSlash(configuration().applicationPath) + this.suggestRouteForDocument(model, databases);
        }
    });

    return CoreVent;

});
