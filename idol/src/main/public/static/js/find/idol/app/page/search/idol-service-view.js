/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/configuration',
    'find/app/page/search/service-view',
    'find/idol/app/page/search/results/idol-results-view-augmentation',
    'find/idol/app/page/search/results/idol-results-view',
    'find/app/util/model-any-changed-attribute-listener'
], function(_, configuration, ServiceView, ResultsViewAugmentation, ResultsView, addChangeListener) {
    'use strict';

    return ServiceView.extend({
        ResultsViewAugmentation: ResultsViewAugmentation,
        ResultsView: ResultsView,
        mapViewResultsStep: configuration().map.resultsStep,
        mapViewAllowIncrement: true,

        initialize: function(options) {
            this.comparisonModalCallback = options.comparisonModalCallback;

            ServiceView.prototype.initialize.call(this, options);

            addChangeListener(this, this.queryModel, ['queryText', 'indexes', 'fieldText', 'minDate', 'maxDate', 'minScore', 'stateMatchIds'], this.fetchData);
        },

        fetchParametricFields: function (fieldsCollection, callback) {
            fieldsCollection.fetch({
                success: _.bind(function() {
                    if (callback) {
                        callback();
                    }
                }, this)
            });
        },
        
        getSavedSearchControlViewOptions: function () {
            return {
                comparisonModalCallback: this.comparisonModalCallback
            };
        },
        
        fetchParametricValues: function () {
            this.parametricCollection.reset();

            var fieldNames = this.parametricFieldsCollection.pluck('id');
            if (fieldNames.length > 0) {
                this.parametricCollection.fetch({data: {
                    fieldNames: fieldNames
                }});
            }
        }
    });

});
