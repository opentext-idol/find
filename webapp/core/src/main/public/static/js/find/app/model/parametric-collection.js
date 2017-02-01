/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/model/find-base-collection',
    'find/app/configuration',
    'underscore'
], function(Backbone, FindBaseCollection, configuration, _) {
    'use strict';

    return FindBaseCollection.extend({
        model: Backbone.Model.extend({
            defaults: {
                values: [],
                dataType: 'parametric'
            }
        }),

        initialize: function(models, options) {
            this.url = options.url;
        },

        parse: function(response) {
            return _.map(response, function(attributes) {
                var fieldMap = _.findWhere(configuration().parametricDisplayValues, {name: attributes.id});

                if(!fieldMap) {
                    return attributes;
                }

                var values = _.map(attributes.values, function(value) {
                    var param = _.findWhere(fieldMap.values, {name: value.value});

                    return param ? _.extend(value, {displayName: param.displayName}) : value;
                });

                return _.extend(attributes, {
                    displayName: fieldMap.displayName,
                    values: values
                });
            });
        }
    });
});
