/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/find-base-collection'
], function(FindBaseCollection) {

    'use strict';

    var URL_ROOT = '../api/public/parametric/buckets';

    var Model = FindBaseCollection.Model.extend({
        urlRoot: URL_ROOT,

        defaults: {
            values: []
        }
    });

    return FindBaseCollection.extend({
        url: URL_ROOT,
        model: Model
    }, {
        Model: Model
    });

});
