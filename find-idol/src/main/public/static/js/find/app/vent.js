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

    function IdolVent(router) {
        VentConstructor.call(this, router);
    }

    function suffixForDocument(documentModel) {
        return [documentModel.get('index'), documentModel.get('reference')]
            .map(encodeURIComponent)
            .join('/');
    }

    IdolVent.prototype = Object.create(VentConstructor.prototype);

    _.extend(IdolVent.prototype, {
        constructor: IdolVent,

        navigateToDetailRoute: function(model) {
            this.navigate('find/search/document/' + suffixForDocument(model));
        },

        navigateToSuggestRoute: function(model) {
            this.navigate('find/search/suggest/' + suffixForDocument(model));
        }
    });

    return new IdolVent(router);

});
