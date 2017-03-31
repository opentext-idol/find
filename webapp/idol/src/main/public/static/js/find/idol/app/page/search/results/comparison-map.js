/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'backbone',
    'find/idol/app/model/comparison/comparison-documents-collection',
    'find/app/page/search/results/state-token-strategy',
    'find/app/page/search/results/map-view',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'i18n!find/idol/nls/comparisons',
    'find/app/page/search/results/add-links-to-summary',
    'find/app/util/search-data-util',
    'text!find/templates/app/page/loading-spinner.html',
    'text!find/idol/templates/comparison/map-comparison-view.html',
    'text!find/templates/app/page/search/results/map-popover.html',
    'find/app/vent',
    'underscore',
    'iCheck'
], function ($, Backbone, ComparisonDocumentsCollection, stateTokenStrategy, MapView,
             configuration, i18n, comparisonsI18n, addLinksToSummary,
             searchDataUtil, loadingSpinnerTemplate, template, popoverTemplate, vent, _) {
    'use strict';

    return Backbone.View.extend({
        className: 'service-view-container',
        template: _.template(template),
        loadingTemplate: _.template(loadingSpinnerTemplate)({i18n: i18n, large: false}),
        popoverTemplate: _.template(popoverTemplate),

        events: {
            'click .map-popup-title': function (e) {
                const allCollections = _.chain(this.comparisons).pluck('collection').pluck('models').flatten().value();
                vent.navigateToDetailRoute(_.findWhere(allCollections, {cid: e.currentTarget.getAttribute('cid')}));
            }
        },

        initialize: function (options) {
            this.searchModels = options.searchModels;
            this.resultsStep = configuration().map.resultsStep;

            this.mapView = new MapView({addControl: true});

            const firstQueryModel = this.createQueryModel(this.model.get('firstText'), this.model.get('onlyInFirst'), [this.searchModels.first]);
            const bothQueryModel = this.createQueryModel(this.model.get('bothText'), this.model.get('inBoth'), [this.searchModels.first, this.searchModels.second]);
            const secondQueryModel = this.createQueryModel(this.model.get('secondText'), this.model.get('onlyInSecond'), [this.searchModels.second]);

            this.comparisons = [
                {
                    name: comparisonsI18n['list.title.first'](this.searchModels.first.get('title')),
                    collection: new ComparisonDocumentsCollection(),
                    layerOptions: {
                        iconCreateFunction: this.mapView.getDivIconCreateFunction('first-location-cluster')
                    },
                    model: firstQueryModel,
                    color: 'green',
                    markers: {}
                },
                {
                    name: comparisonsI18n['list.title.both'],
                    collection: new ComparisonDocumentsCollection(),
                    layerOptions: {
                        iconCreateFunction: this.mapView.getDivIconCreateFunction('both-location-cluster')
                    },
                    model: bothQueryModel,
                    color: 'orange',
                    markers: {}
                },
                {
                    name: comparisonsI18n['list.title.second'](this.searchModels.second.get('title')),
                    collection: new ComparisonDocumentsCollection(),
                    layerOptions: {
                        iconCreateFunction: this.mapView.getDivIconCreateFunction('second-location-cluster')
                    },
                    model: secondQueryModel,
                    color: 'red',
                    markers: {}
                }
            ];

            this.parentLayerModel = new Backbone.Model();

            this.createAddListeners();
            this.createSyncListeners();
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

            this.mapView.setElement(this.$('.location-comparison-map')).render();

            this.$('.location-comparison-show-more').click(function () {
                this.fetchDocuments();
            }.bind(this));

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

        createAddListeners: function () {
            this.comparisons.forEach(function (comparison) {
                this.listenTo(comparison.collection, 'add', function (model) {
                    const locations = model.get('locations');
                    Object.keys(locations).forEach(function (locationName) {
                        locations[locationName].forEach(function (location) {
                            const longitude = location.longitude;
                            const latitude = location.latitude;
                            const title = model.get('title');
                            const popover = this.popoverTemplate({
                                title: title,
                                i18n: i18n,
                                summary: addLinksToSummary(model.get('summary')),
                                cidForClickRouting: model.cid
                            });
                            const icon = this.mapView.getIcon(location.iconName, location.iconColor, comparison.color);
                            const marker = this.mapView.getMarker(latitude, longitude, icon, title, popover);

                            if (comparison.markers[location.displayName]) {
                                comparison.markers[location.displayName].push(marker);
                            } else {
                                comparison.markers[location.displayName] = [marker];
                            }
                        }, this);
                    }, this);
                });
            }, this);
        },

        createSyncListeners: function () {
            this.comparisons.forEach(function (comparison) {
                this.listenTo(comparison.collection, 'sync', function () {
                    if (!_.isEmpty(comparison.markers)) {
                        Object.keys(comparison.markers).forEach(function (markerName) {
                            let parentLayer = this.parentLayerModel.get(markerName);
                            if (!parentLayer) {
                                parentLayer = this.mapView.addGroupingLayer(markerName);
                                this.parentLayerModel.set(markerName, parentLayer);
                            }
                            this.mapView.addMarkers(comparison.markers[markerName], {
                                clusterLayer: comparison.clusterLayer,
                                groupingLayer: parentLayer
                            });
                        }, this);
                        this.mapView.fitMapToMarkerBounds();
                    }
                    this.toggleLoading()
                });
            }, this)
        },

        reloadMarkers: function () {
            if (this.mapView.mapRendered()) {
                this.clearMarkers();
                this.comparisons.forEach(function (comparison) {
                    comparison.clusterLayer = this.mapView.addClusterLayer(comparison.name, comparison.layerOptions);
                }, this);
                this.fetchDocuments();
            }
        },

        clearMarkers: function () {
            this.$('.map-results-count').empty();
            this.mapView.clearMarkers();
            this.comparisons.forEach(function (comparison) {
                comparison.collection.reset();
                comparison.clusterLayer = null;
                comparison.markers = {};
            });
            this.parentLayerModel.clear();
        },

        collectionsFetching: function () {
            return _.chain(this.comparisons).pluck('collection').pluck('fetching').some().value();
        },

        collectionsFull: function () {
            return _.chain(this.comparisons)
                .pluck('collection')
                .reject(function (collection) {
                    return collection.length === collection.totalResults
                })
                .isEmpty()
                .value();
        },

        toggleLoading: function () {
            this.$loadingSpinner.toggleClass('hide', !this.collectionsFetching());
            this.$('.location-comparison-show-more').prop('disabled', this.collectionsFetching() || this.collectionsFull());
        },

        getFetchOptions: function (queryModel, fieldText, length) {
            const newFieldText = queryModel.get('fieldText')
                ? '(' + queryModel.get('fieldText') + ') AND (' + fieldText + ')'
                : fieldText;

            return {
                data: _.extend({
                    start: length + 1,
                    max_results: length + this.resultsStep,
                    field_text: newFieldText,
                    sort: 'relevance',
                    summary: 'context'
                }, stateTokenStrategy.requestParams(queryModel)),
                remove: false,
                reset: false
            };
        },

        fetchDocuments: function () {
            const config = configuration();

            const locationFields = config.map.locationFields;
            if (!_.isEmpty(locationFields)) {
                const fieldText = locationFields.map(function (locationField) {
                    //noinspection JSUnresolvedVariable
                    return '(EXISTS{}:' + config.fieldsInfo[locationField.latitudeField].names.join(':') +
                        ' AND EXISTS{}:' + config.fieldsInfo[locationField.longitudeField].names.join(':') + ')';
                }).join(' OR ');

                this.comparisons.forEach(function (comparison) {
                    const options = this.getFetchOptions(comparison.model, fieldText, comparison.collection.length);
                    comparison.collection.fetch(options);
                }, this);
                this.toggleLoading();
            }
        }
    });
});
