/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'i18n!find/idol/nls/comparisons',
    'find/app/configuration',
    'find/idol/app/page/search/comparison/compare-modal',
    'find/app/page/search/service-view',
    'find/idol/app/page/search/results/idol-results-view-augmentation',
    'find/idol/app/page/search/results/idol-results-view',
    'find/app/util/model-any-changed-attribute-listener'
], function(_, comparisonsI18n, configuration, CompareModal, ServiceView, ResultsViewAugmentation, ResultsView, addChangeListener) {

    'use strict';

    return ServiceView.extend({
        ResultsViewAugmentation: ResultsViewAugmentation,
        ResultsView: ResultsView,
        mapViewResultsStep: configuration().map.resultsStep,
        mapViewAllowIncrement: true,

        events: _.extend({
            'click .compare-modal-button': function() {
                new CompareModal({
                    savedSearchCollection: this.savedSearchCollection,
                    selectedSearch: this.savedSearchModel,
                    comparisonSuccessCallback: this.comparisonSuccessCallback
                });
            }
        }, ServiceView.prototype.events),

        initialize: function(options) {
            this.comparisonSuccessCallback = options.comparisonSuccessCallback;
            this.listenTo(this.savedSearchCollection, 'reset update', this.updateCompareModalButton);

            if (configuration().hasBiRole) {
                this.headerControlsHtml = _.template('<button class="btn button-primary compare-modal-button"><%-i18n["compare"]%></button>')({i18n: comparisonsI18n})
            }

            ServiceView.prototype.initialize.call(this, options);

            addChangeListener(this, this.queryModel, ['queryText', 'indexes', 'fieldText', 'minDate', 'maxDate', 'stateMatchIds'], this.fetchData);
        },

        render: function() {
            ServiceView.prototype.render.call(this);
            this.updateCompareModalButton();
        },

        updateCompareModalButton: function() {
            this.$('.compare-modal-button').toggleClass('disabled not-clickable', this.savedSearchCollection.length <= 1);
        },

        fetchParametricFields: function (fieldsCollection, valuesCollection) {
            fieldsCollection.fetch({
                success: _.bind(function() {
                    if (valuesCollection) {
                        this.fetchParametricValues(fieldsCollection, valuesCollection);
                    }
                }, this)
            });
        }
    });

});
