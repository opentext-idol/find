/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/model/find-base-collection'
], function(Backbone, FindBaseCollection) {
    'use strict';

    return FindBaseCollection.extend({
        model: Backbone.Model.extend({
            defaults: {
                values: [],
                type: 'Parametric'
            }
        }),

        initialize: function(models, options) {
            this.url = options.url;
        }
    });
});
