/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'find/app/page/search/results/map-results-view-strategy',
    'find/app/page/search/results/map-view',
    'i18n!find/nls/bundle',
    'find/app/model/documents-collection',
    'text!find/templates/app/page/search/results/map-results-view.html',
    'text!find/templates/app/page/search/results/map-popover.html',
    'text!find/templates/app/page/loading-spinner.html',
    'find/app/vent'
], function (_, $, Backbone, mapResultsViewStrategy, MapView, i18n, DocumentsCollection,
             template, popoverTemplate, loadingSpinnerTemplate, vent) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(template),
        popoverTemplate: _.template(popoverTemplate),
        loadingTemplate: _.template(loadingSpinnerTemplate)({i18n: i18n, large: false}),

        events: {
            'click .map-show-more': function () {
                this.mapResultsViewStrategy.fetchDocuments()
            },
            'click .map-popup-title': function (e) {
                //noinspection JSUnresolvedFunction
                vent.navigateToDetailRoute(this.documentsCollection.get(e.currentTarget.getAttribute('cid')));
            }
        },

        initialize: function (options) {
            this.documentsCollection = new DocumentsCollection();
            const resultSets = [{
                collection: this.documentsCollection,
                model: options.queryModel,
                markers: {}
            }];

            this.resultsStep = options.resultsStep;
            this.mapResultsViewStrategy = mapResultsViewStrategy({
                allowIncrement: options.allowIncrement,
                resultsStep: this.resultsStep,
                clusterMarkers: true,
                popoverTemplate: this.popoverTemplate,
                mapViewOptions: {addControl: true},
                resultSets: resultSets,
                toggleLoading: this.toggleLoading.bind(this)
            });

            this.mapResultsViewStrategy.createAddListeners(this.listenTo.bind(this));
            this.mapResultsViewStrategy.createSyncListeners(this.listenTo.bind(this), function () {
                this.$('.map-results-count').html(this.getResultsNoHTML());
            }.bind(this));

            this.listenTo(options.queryModel, 'change', this.reloadMarkers);
        },

        render: function () {
            this.$el.html(this.template({
                showMore: i18n['search.resultsView.map.show.more']
            }));
            this.mapResultsViewStrategy.mapView.setElement(this.$('.location-results-map')).render();
            this.$loadingSpinner = $(this.loadingTemplate);
            this.$loadMoreButton = this.$('.map-show-more');
            if (!this.allowIncrement) {
                this.$loadMoreButton.addClass('hide disabled');
            }
            this.$('.map-loading-spinner').html(this.$loadingSpinner);

            this.toggleLoading();
            this.$loadMoreButton.prop('disabled', true);

            this.reloadMarkers();
        },

        getResultsNoHTML: function () {
            return this.documentsCollection.isEmpty()
                ? i18n['search.resultsView.amount.shown.no.results']
                : this.allowIncrement
                    ? i18n['search.resultsView.amount.shown'](1, this.documentsCollection.length, this.documentsCollection.totalResults)
                    : i18n['search.resultsView.amount.shown.no.increment'](this.resultsStep, this.documentsCollection.totalResults);
        },

        reloadMarkers: function () {
            this.$('.map-results-count').empty();
            this.mapResultsViewStrategy.reloadMarkers();
        },

        toggleLoading: function () {
            this.$loadingSpinner.toggleClass('hide', !this.mapResultsViewStrategy.collectionsFetching());
            this.$loadMoreButton.prop('disabled', this.mapResultsViewStrategy.collectionsFetching() || this.mapResultsViewStrategy.collectionsFull());
        }
    });
});
