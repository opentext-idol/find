/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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

        fetchFromQueryModel: function (queryModel, data, options) {
            return this.fetch(_.defaults({
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
            }, options));
        }
    });
});
