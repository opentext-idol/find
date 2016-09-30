/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone'
], function(Backbone) {
    'use strict';

    return Backbone.Collection.extend({

        comparator: 'id',

        url: function() {
            return '../api/admin/customisation/assets/' + this.type
        },

        initialize: function(models, options) {
            this.type = options.type;
        },

        parse: function(data) {
            return _.map(data, function(datum) {
                return {id: datum};
            });
        }

    });

});