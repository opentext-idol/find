/*
 * Copyright 2015-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

const configuration = require('find/app/configuration');
const ServiceView = require('find/app/page/search/service-view');
const RecommendView = require('find/idol/app/page/search/results/idol-recommend-view');
const ResultsViewAugmentation = require('find/idol/app/page/search/results/idol-results-view-augmentation');
const ResultsView = require('find/idol/app/page/search/results/idol-results-view');
const addChangeListener = require('js-whatever/js/model-any-changed-attribute-listener');
const ParametricFieldsCollection = require('find/app/model/parametric-fields-collection');

module.exports = ServiceView.extend({
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

