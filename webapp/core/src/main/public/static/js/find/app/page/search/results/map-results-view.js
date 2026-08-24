/*
 * Copyright 2016-2017 Open Text.
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

const _ = require('underscore');
const $ = require('jquery');
const Backbone = require('backbone');
const mapResultsViewStrategy = require('find/app/page/search/results/map-results-view-strategy');
const MapView = require('find/app/page/search/results/map-view');
const i18n = require('find/nls/bundle');
const DocumentsCollection = require('find/app/model/documents-collection');
const template = require('find/templates/app/page/search/results/map-results-view.html');
const popoverTemplate = require('find/templates/app/page/search/results/map-popover.html');
const loadingSpinnerTemplate = require('find/templates/app/page/loading-spinner.html');
const vent = require('find/app/vent');
const generateErrorHtml = require('find/app/util/generate-error-support-message');

const popoverTemplateFn = _.template(popoverTemplate);
const loadingHtml = _.template(loadingSpinnerTemplate)({i18n: i18n, large: false});

module.exports = Backbone.View.extend({
        template: _.template(template),

        events: {
            'click .map-show-more': function() {
                this.mapResultsViewStrategy.fetchDocuments()
            },
            'click .map-popup-title': function(e) {
                vent.navigateToDetailRoute(this.documentsCollection.get(e.currentTarget.getAttribute('cid')));
            }
        },

        initialize: function(options) {
            this.documentsCollection = new DocumentsCollection();
            const resultSets = [{
                collection: this.documentsCollection,
                model: options.queryModel,
                markers: {}
            }];

            this.allowIncrement = options.allowIncrement;
            this.resultsStep = options.resultsStep;

            this.errorModel = new Backbone.Model();

            this.mapResultsViewStrategy = mapResultsViewStrategy({
                allowIncrement: this.allowIncrement,
                resultsStep: this.resultsStep,
                clusterMarkers: true,
                popoverTemplate: popoverTemplateFn,
                mapViewOptions: {addControl: true},
                resultSets: resultSets,
                toggleLoading: this.toggleLoading.bind(this),
                errorModel: this.errorModel,
                errorCallback: function(errorInfo) {
                    this.$error.html(generateErrorHtml(errorInfo.hasError
                        ? {
                            errorDetails: errorInfo.responseJSON.message,
                            errorUUID: errorInfo.responseJSON.uuid,
                            errorLookup: errorInfo.responseJSON.backendErrorCode,
                            isUserError: errorInfo.responseJSON.isUserError
                        }
                        : {}));
                    this.$error.toggleClass('hide', !errorInfo.hasError);
                    this.toggleLoading();
                }.bind(this)
            });

            this.mapResultsViewStrategy.createAddListeners(this.listenTo.bind(this));
            this.mapResultsViewStrategy.createSyncListeners(this.listenTo.bind(this), function() {
                this.$('.map-results-count').html(this.getResultsNoHTML());
            }.bind(this));
            this.mapResultsViewStrategy.listenForErrors(this.listenTo.bind(this));

            this.listenTo(options.queryModel, 'change', this.reloadMarkers);
        },

        render: function() {
            this.$el.html(this.template({
                showMore: i18n['search.resultsView.map.show.more']
            }));
            this.mapResultsViewStrategy.mapView.setElement(this.$('.location-results-map')).render();
            this.$loadingSpinner = $(loadingHtml);
            this.$loadMoreButton = this.$('.map-show-more');
            if(!this.allowIncrement) {
                this.$loadMoreButton.addClass('hide disabled');
            }
            this.$('.map-loading-spinner').html(this.$loadingSpinner);
            this.$error = this.$('.map-error');

            this.toggleLoading();
            this.$loadMoreButton.prop('disabled', true);

            this.reloadMarkers();
        },

        getResultsNoHTML: function() {
            return this.documentsCollection.isEmpty()
                ? i18n['search.resultsView.amount.shown.no.results']
                : this.allowIncrement
                       ? i18n['search.resultsView.amount.shown'](1, this.documentsCollection.length, this.documentsCollection.totalResults)
                       : i18n['search.resultsView.amount.shown.no.increment'](this.resultsStep, this.documentsCollection.totalResults);
        },

        reloadMarkers: function() {
            this.$('.map-results-count').empty();
            this.mapResultsViewStrategy.reloadMarkers();
        },

        toggleLoading: function() {
            this.$loadingSpinner.toggleClass('hide', !this.mapResultsViewStrategy.collectionsFetching());
            this.$loadMoreButton.prop('disabled',
                !!(this.mapResultsViewStrategy.collectionsFetching() ||
                this.mapResultsViewStrategy.collectionsFull() ||
                this.errorModel.get('hasError')));
        }
    });

