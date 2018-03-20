/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/configuration',
    'find/app/page/search/service-view',
    'find/idol/app/page/search/results/idol-recommend-view',
    'find/idol/app/page/search/results/idol-results-view-augmentation',
    'find/idol/app/page/search/results/idol-results-view',
    'js-whatever/js/model-any-changed-attribute-listener',
    'find/app/model/parametric-fields-collection'
], function(configuration, ServiceView, RecommendView, ResultsViewAugmentation, ResultsView, addChangeListener,
            ParametricFieldsCollection) {
    'use strict';

    return ServiceView.extend({
        RecommendView: RecommendView,
        ResultsViewAugmentation: ResultsViewAugmentation,
        ResultsView: ResultsView,
        mapViewResultsStep: configuration().map.resultsStep,
        mapViewAllowIncrement: true,
        parametricFieldsCollection: new ParametricFieldsCollection([]),

        initialize: function(options) {
            this.comparisonModalCallback = options.comparisonModalCallback;

            ServiceView.prototype.initialize.call(this, options);

            addChangeListener(
                this,
                this.queryModel,
                [
                    'queryText',
                    'indexes',
                    'fieldText',
                    'minDate',
                    'maxDate',
                    'minScore',
                    'stateMatchIds'
                ],
                this.fetchData
            );

            this.listenTo(this.parametricFieldsCollection, 'sync', this.fetchParametricCollection);
        },

        fetchParametricFields: function() {
            if(this.parametricFieldsCollection.isEmpty()) {
                this.parametricFieldsCollection.fetch({
                    data: {
                        fieldTypes: ['Parametric', 'Numeric', 'NumericDate']
                    }
                });
            } else {
                this.fetchParametricCollection();
            }
        },

        getSavedSearchControlViewOptions: function() {
            return {
                comparisonModalCallback: this.comparisonModalCallback,
                resultsViewSelectionModel: this.resultsViewSelectionModel
            };
        }
    });
});
