/*
 * Copyright 2016-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    './core-vent',
    'find/app/configuration',
    'find/app/router'
], function(_, CoreVent, configuration, router) {

    'use strict';

    function HodVent(router) {
        CoreVent.call(this, router);
    }

    HodVent.prototype = Object.create(CoreVent.prototype);

    _.extend(HodVent.prototype, {
        constructor: HodVent,

        addSuffixForDocument: function(model) {
            return [model.get('domain'), model.get('index'), model.get('reference')]
                .map(encodeURIComponent)
                .join('/');
        }
    });

    return new HodVent(router);

});
