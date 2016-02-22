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

    HodVent.prototype = Object.create(VentConstructor.prototype);

    _.extend(HodVent.prototype, {
        constructor: HodVent,

        navigateToDetailRoute: function (model) {
            var domain = encodeURIComponent(model.get('domain'));
            var index = encodeURIComponent(model.get('index'));
            var reference = encodeURIComponent(model.get('reference'));
            this.navigate('find/search/document/' + domain + '/' + index + '/' + reference);
        }
    });

    return new HodVent(router);

});
