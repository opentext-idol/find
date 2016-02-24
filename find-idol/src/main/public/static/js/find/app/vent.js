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

    IdolVent.prototype = Object.create(VentConstructor.prototype);

    _.extend(IdolVent.prototype, {
        constructor: IdolVent,

        navigateToDetailRoute: function (model) {
            var database = encodeURIComponent(model.get('index'));
            var reference = encodeURIComponent(model.get('reference'));
            this.navigate('find/search/document/' + database + '/' + reference);
        }
    });

    return new IdolVent(router);

});
