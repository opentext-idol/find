/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'backbone',
    'find/idol/app/model/comparison/comparison-documents-collection',
    'find/app/page/search/results/map-results-view-strategy',
    'find/app/page/search/results/map-view',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'i18n!find/idol/nls/comparisons',
    'find/app/util/search-data-util',
    'text!find/templates/app/page/loading-spinner.html',
    'text!find/idol/templates/comparison/map-comparison-view.html',
    'text!find/templates/app/page/search/results/map-popover.html',
    'find/app/vent',
    'underscore',
    'iCheck'
], function ($, Backbone, ComparisonDocumentsCollection, mapResultsViewStrategy, MapView, configuration, i18n, comparisonsI18n,
             searchDataUtil, loadingSpinnerTemplate, template, popoverTemplate, vent, _) {
    'use strict';

    return Backbone.View.extend({
        className: 'service-view-container',
        template: _.template(template),
        loadingTemplate: _.template(loadingSpinnerTemplate)({i18n: i18n, large: false}),
        popoverTemplate: _.template(popoverTemplate),

        events: {
            'click .location-comparison-show-more': function () {
                this.mapResultsViewStrategy.fetchDocuments()
            },

            'click .map-popup-title': function (e) {
                const allCollections = _.chain(this.comparisons).pluck('collection').pluck('models').flatten().value();
                vent.navigateToDetailRoute(_.findWhere(allCollections, {cid: e.currentTarget.getAttribute('cid')}));
            }
        },

        initialize: function (options) {
            this.searchModels = options.searchModels;

            const firstQueryModel = this.createQueryModel(this.model.get('firstText'), this.model.get('onlyInFirst'), [this.searchModels.first]);
            const bothQueryModel = this.createQueryModel(this.model.get('bothText'), this.model.get('inBoth'), [this.searchModels.first, this.searchModels.second]);
            const secondQueryModel = this.createQueryModel(this.model.get('secondText'), this.model.get('onlyInSecond'), [this.searchModels.second]);

            const resultSets = [
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

            this.mapResultsViewStrategy = mapResultsViewStrategy({
                allowIncrement: true,
                resultsStep: configuration().map.resultsStep,
                clusterMarkers: true,
                popoverTemplate: this.popoverTemplate,
                mapViewOptions: {addControl: true},
                resultSets: resultSets,
                toggleLoading: this.toggleLoading.bind(this)
            });

            resultSets.forEach(function (resultSet) {
                resultSet.collection = new ComparisonDocumentsCollection();
                resultSet.layerOptions = {
                    iconCreateFunction: this.mapResultsViewStrategy.mapView.getDivIconCreateFunction(resultSet.iconClass)
                };
                resultSet.markers = {};
            }, this);

            this.mapResultsViewStrategy.createAddListeners(this.listenTo.bind(this));
            this.mapResultsViewStrategy.createSyncListeners(this.listenTo.bind(this));
        },

        render: function () {
            this.$el.html(this.template({
                bothLabel: comparisonsI18n['list.title.both'],
                firstLabel: comparisonsI18n['list.title.first'](this.searchModels.first.get('title')),
                secondLabel: comparisonsI18n['list.title.second'](this.searchModels.second.get('title')),
                showMore: i18n['search.resultsView.map.show.more']
            }));

            this.$loadingSpinner = $(this.loadingTemplate);
            this.$('.map-loading-spinner').html(this.$loadingSpinner);

            this.mapResultsViewStrategy.mapView.setElement(this.$('.location-comparison-map')).render();

            this.toggleLoading();
            this.reloadMarkers();
        },

        createQueryModel: function (queryText, stateTokens, searchModels) {
            const indexes = _.chain(searchModels)
                .map(function (model) {
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

        reloadMarkers: function () {
            this.$('.map-results-count').empty();
            this.mapResultsViewStrategy.reloadMarkers();
        },

        toggleLoading: function () {
            this.$loadingSpinner.toggleClass('hide', !this.mapResultsViewStrategy.collectionsFetching());
            this.$('.location-comparison-show-more').prop('disabled', this.mapResultsViewStrategy.collectionsFetching() || this.mapResultsViewStrategy.collectionsFull());
        }
    });
});
