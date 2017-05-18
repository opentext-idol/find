/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/find-base-collection'
], function(FindBaseCollection) {
    'use strict';

    const URL_ROOT = 'api/public/parametric/date/buckets';

    const Model = FindBaseCollection.Model.extend({
        urlRoot: URL_ROOT,

        url: function() {
            const base = this.collection ? this.collection.url() : URL_ROOT;
            // Double encode since Spring doesn't like %2F in URLs
            return this.isNew()
                ? base
                : base.replace(/[^\/]$/, '$&/') + encodeURIComponent(encodeURIComponent(this.id));
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
