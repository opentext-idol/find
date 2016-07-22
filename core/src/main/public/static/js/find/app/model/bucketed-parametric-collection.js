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

        url: function() {
            var base = this.collection ? this.collection.url() : URL_ROOT;
            // Double encode since Spring doesn't like %2F in URLs
            return this.isNew() ? base : base.replace(/[^\/]$/, '$&/') + encodeURIComponent(encodeURIComponent(this.id));
        },

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
