/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone'
], function(_, Backbone) {
    'use strict';

    return Backbone.Collection.extend({
        comparator: 'id',

        url: function() {
            return 'api/admin/customization/assets/' + this.type
        },

        initialize: function(models, options) {
            this.type = options.type;
        },

        parse: function(data) {
            return _.map(data, function(datum) {
                return {id: datum};
            });
        },

        model: Backbone.Model.extend({
            defaults: {
                deletable: true
            }
        })
    });
});
