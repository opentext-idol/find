/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'js-whatever/js/vent-constructor',
    'find/app/router'
], function(_, VentConstructor, router) {

    'use strict';

    function HodVent(router) {
        VentConstructor.call(this, router);
    }

    function suffixForDocument(documentModel) {
        return [documentModel.get('domain'), documentModel.get('index'), documentModel.get('reference')]
            .map(encodeURIComponent)
            .join('/');
    }

    HodVent.prototype = Object.create(VentConstructor.prototype);

    _.extend(HodVent.prototype, {
        constructor: HodVent,

        navigateToDetailRoute: function(model) {
            this.navigate('find/search/document/' + hodSuffixForDocument(model));
        },

        navigateToSuggestRoute: function(model) {
            this.navigate('find/search/suggest/' + hodSuffixForDocument(model));
        }
    });

    return new HodVent(router);

});
