/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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

        suggestRouteForDocument: function(model) {
            return '/search/suggest/' + this.addSuffixForDocument(model);
        },

        suggestUrlForDocument: function(model) {
            return stripLeadingSlash(configuration().applicationPath) + this.suggestRouteForDocument(model);
        }
    });

    return CoreVent;

});
