/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone',
    'find/app/model/find-base-collection'
], function(_, Backbone, FindBaseCollection) {
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
        },

        fetchFromQueryModel: function (queryModel, data) {
            return this.fetch({
                data: _.extend({
                    databases: queryModel.get('indexes'),
                    queryText: queryModel.get('autoCorrect') && queryModel.get('correctedQuery')
                        ? queryModel.get('correctedQuery')
                        : queryModel.get('queryText'),
                    fieldText: queryModel.get('fieldText'),
                    minDate: queryModel.getIsoDate('minDate'),
                    maxDate: queryModel.getIsoDate('maxDate'),
                    minScore: queryModel.get('minScore'),
                    stateTokens: queryModel.get('stateMatchIds')
                }, data)
            });
        }
    });
});
