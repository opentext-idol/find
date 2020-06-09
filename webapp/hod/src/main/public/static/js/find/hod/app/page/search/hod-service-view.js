/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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
