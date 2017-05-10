/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/service-view',
    'find/hod/app/page/search/results/hod-results-view-augmentation',
    'find/hod/app/page/search/results/hod-results-view',
    'js-whatever/js/model-any-changed-attribute-listener'
], function(ServiceView, ResultsViewAugmentation, ResultsView, addChangeListener) {
    'use strict';

    return ServiceView.extend({
        ResultsView: ResultsView,
        ResultsViewAugmentation: ResultsViewAugmentation,

        // TODO: Enable sunburst in HOD when IOD-9173 is complete
        displayDependentParametricViews: false,
        mapViewResultsStep: 2500,
        mapViewAllowIncrement: false,

        initialize: function(options) {
            ServiceView.prototype.initialize.call(this, options);

            addChangeListener(this, this.queryModel,
                ['queryText', 'fieldText', 'minDate', 'maxDate', 'minScore', 'stateMatchIds'],
                this.fetchData);

            addChangeListener(this, this.queryModel, ['indexes'], function() {
                if(this.entityCollection) {
                    this.fetchEntities();
                }

                this.parametricFieldsCollection.reset();
                this.fetchParametricFields();
            });

            this.listenTo(this.queryModel, 'change:indexes', function() {
                this.queryState.selectedParametricValues.reset();
            });
        },

        fetchParametricFields: function() {
            if(this.queryModel.get('indexes').length > 0) {
                this.parametricFieldsCollection.fetch({
                    data: {
                        fieldTypes: ['Parametric', 'Numeric', 'NumericDate'],
                        databases: this.queryModel.get('indexes')
                    },
                    success: this.fetchParametricCollection.bind(this)
                });
            }
        }
    });
});
