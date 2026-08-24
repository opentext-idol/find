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
const ComparisonDocumentsCollection = require('find/idol/app/model/comparison/comparison-documents-collection');
const mapResultsViewStrategy = require('find/app/page/search/results/map-results-view-strategy');
const MapView = require('find/app/page/search/results/map-view');
const configuration = require('find/app/configuration');
const i18n = require('find/nls/bundle');
const comparisonsI18n = require('find/idol/nls/comparisons');
const searchDataUtil = require('find/app/util/search-data-util');
const loadingSpinnerTemplate = require('find/templates/app/page/loading-spinner.html');
const template = require('find/idol/templates/comparison/map-comparison-view.html');
const popoverTemplate = require('find/templates/app/page/search/results/map-popover.html');
const vent = require('find/app/vent');
const generateErrorHtml = require('find/app/util/generate-error-support-message');

// Loaded for side effects only - do not remove.
require('iCheck');

const popoverTemplateFn = _.template(popoverTemplate);
const loadingHtml = _.template(loadingSpinnerTemplate)({i18n: i18n, large: false});

module.exports = Backbone.View.extend({
        className: 'service-view-container',
        template: _.template(template),

        events: {
            'click .location-comparison-show-more': function() {
                this.mapResultsViewStrategy.fetchDocuments()
            },

            'click .map-popup-title': function(e) {
                const allCollections = _.chain(this.comparisons).pluck('collection').pluck('models').flatten().value();
                vent.navigateToDetailRoute(_.findWhere(allCollections, {cid: e.currentTarget.getAttribute('cid')}));
            }
        },

        initialize: function(options) {
            this.searchModels = options.searchModels;

            const firstQueryModel = this.createQueryModel(this.model.get('firstText'), this.model.get('onlyInFirst'), [this.searchModels.first]);
            const bothQueryModel = this.createQueryModel(this.model.get('bothText'), this.model.get('inBoth'), [this.searchModels.first, this.searchModels.second]);
            const secondQueryModel = this.createQueryModel(this.model.get('secondText'), this.model.get('onlyInSecond'), [this.searchModels.second]);

            this.resultSets = [
                {
                    name: comparisonsI18n['list.title.first'](this.searchModels.first.get('title')),
                    iconClass: 'first-location-cluster',
                    model: firstQueryModel,
                    color: 'green'
                },
                {
                    name: comparisonsI18n['list.title.both'],
                    iconClass: 'both-location-cluster',
                    model: bothQueryModel,
                    color: 'orange'
                },
                {
                    name: comparisonsI18n['list.title.second'](this.searchModels.second.get('title')),
                    iconClass: 'second-location-cluster',
                    model: secondQueryModel,
                    color: 'red'
                }
            ];

            this.errorModel = new Backbone.Model();

            this.mapResultsViewStrategy = mapResultsViewStrategy({
                allowIncrement: true,
                resultsStep: configuration().map.resultsStep,
                clusterMarkers: true,
                popoverTemplate: popoverTemplateFn,
                mapViewOptions: {addControl: true},
                resultSets: this.resultSets,
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

            this.resultSets.forEach(function(resultSet) {
                resultSet.collection = new ComparisonDocumentsCollection();
                resultSet.layerOptions = {
                    iconCreateFunction: this.mapResultsViewStrategy.mapView.getDivIconCreateFunction(resultSet.iconClass)
                };
                resultSet.markers = {};
            }, this);

            this.mapResultsViewStrategy.createAddListeners(this.listenTo.bind(this));
            this.mapResultsViewStrategy.createSyncListeners(this.listenTo.bind(this));
            this.mapResultsViewStrategy.listenForErrors(this.listenTo.bind(this));
        },

        render: function() {
            this.$el.html(this.template({
                bothLabel: comparisonsI18n['list.title.both'],
                firstLabel: comparisonsI18n['list.title.first'](this.searchModels.first.get('title')),
                secondLabel: comparisonsI18n['list.title.second'](this.searchModels.second.get('title')),
                showMore: i18n['search.resultsView.map.show.more']
            }));

            this.$loadingSpinner = $(loadingHtml);
            this.$('.map-loading-spinner').html(this.$loadingSpinner);
            this.$error = this.$('.map-error');

            this.mapResultsViewStrategy.mapView.setElement(this.$('.location-comparison-map')).render();

            this.toggleLoading();
            this.reloadMarkers();
        },

        createQueryModel: function(queryText, stateTokens, searchModels) {
            const indexes = _.chain(searchModels)
                .map(function(model) {
                    return searchDataUtil.buildIndexes(model.get('indexes'));
                })
                .flatten()
                .uniq()
                .value();

            return new Backbone.Model(_.extend({
                queryText: queryText,
                indexes: indexes
            }, stateTokens));
        },

        reloadMarkers: function() {
            this.$('.map-results-count').empty();
            this.mapResultsViewStrategy.reloadMarkers();
        },

        toggleLoading: function() {
            this.$loadingSpinner.toggleClass('hide', !this.mapResultsViewStrategy.collectionsFetching());
            this.$('.location-comparison-show-more').prop('disabled',
                !!(this.mapResultsViewStrategy.collectionsFetching() ||
                this.mapResultsViewStrategy.collectionsFull() ||
                this.errorModel.get('hasError'))
            );
        }
    });

