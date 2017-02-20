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
    'find/app/page/search/results/field-selection-view',
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
             FieldSelectionView, configuration, i18n, comparisonsI18n, addLinksToSummary,
             searchDataUtil, loadingSpinnerTemplate, template, popoverTemplate, vent, _) {
    'use strict';

    function locationFieldsToSelectionFields(locationFields) {
        return locationFields.map(function (locationField) {
            return {
                id: locationField.displayName,
                displayName: locationField.displayName
            }
        })
    }

    return Backbone.View.extend({
        className: 'service-view-container',
        template: _.template(template),
        loadingTemplate: _.template(loadingSpinnerTemplate)({i18n: i18n, large: false}),
        popoverTemplate: _.template(popoverTemplate),

        events: {
            'click .map-popup-title': function (e) {
                const allCollections = _.chain(this.comparisons).pluck('collection').pluck('models').flatten().value();
                vent.navigateToDetailRoute(_.findWhere(allCollections, {cid: e.currentTarget.getAttribute('cid')}));
            },
            'click .map-pptx': function(e){
                e.preventDefault();
                this.mapView.exportPPT(
                    '\'' + this.searchModels.first.get('title') + '\' v.s. \'' + this.searchModels.second.get('title') + '\''
                    + '\n' + '(' +  _.unique(_.map([this.firstSelectionView, this.bothSelectionView, this.secondSelectionView], function(view){
                        return view.model.get('displayValue');
                    })).join(', ') + ')'
                )
            }
        },

        initialize: function (options) {
            this.searchModels = options.searchModels;
            this.locationFields = configuration().map.locationFields;
            this.resultsStep = configuration().map.resultsStep;

            this.mapView = new MapView({addControl: true});

            const firstQueryModel = this.createQueryModel(this.model.get('firstText'), this.model.get('onlyInFirst'), [this.searchModels.first]);
            const bothQueryModel = this.createQueryModel(this.model.get('bothText'), this.model.get('inBoth'), [this.searchModels.first, this.searchModels.second]);
            const secondQueryModel = this.createQueryModel(this.model.get('secondText'), this.model.get('onlyInSecond'), [this.searchModels.second]);

            const selectionFields = locationFieldsToSelectionFields(this.locationFields);
            this.firstSelectionView = new FieldSelectionView({
                model: firstQueryModel,
                name: 'FirstFieldSelectionView',
                width: '100%',
                fields: selectionFields,
                allowEmpty: false
            });

            this.bothSelectionView = new FieldSelectionView({
                model: bothQueryModel,
                name: 'BothFieldSelectionView',
                width: '100%',
                fields: selectionFields,
                allowEmpty: false
            });

            this.secondSelectionView = new FieldSelectionView({
                model: secondQueryModel,
                name: 'SecondFieldSelectionView',
                width: '100%',
                fields: selectionFields,
                allowEmpty: false
            });

            this.comparisons = [
                {
                    name: comparisonsI18n['list.title.first'](this.searchModels.first.get('title')),
                    collection: new ComparisonDocumentsCollection(),
                    layer: this.mapView.createLayer({
                        iconCreateFunction: this.mapView.getDivIconCreateFunction('first-location-cluster')
                    }),
                    model: firstQueryModel,
                    color: 'green'
                },
                {
                    name: comparisonsI18n['list.title.both'],
                    collection: new ComparisonDocumentsCollection(),
                    layer: this.mapView.createLayer({
                        iconCreateFunction: this.mapView.getDivIconCreateFunction('both-location-cluster')
                    }),
                    model: bothQueryModel,
                    color: 'orange'
                },
                {
                    name: comparisonsI18n['list.title.second'](this.searchModels.second.get('title')),
                    collection: new ComparisonDocumentsCollection(),
                    layer: this.mapView.createLayer({
                        iconCreateFunction: this.mapView.getDivIconCreateFunction('second-location-cluster')
                    }),
                    model: secondQueryModel,
                    color: 'red'
                }
            ];

            this.createAddListeners(this.comparisons);
            this.createSyncListeners(this.comparisons);
            this.createModelListeners(this.comparisons);
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

            this.$('.first-select-label').append(this.firstSelectionView.$el);
            this.$('.both-select-label').append(this.bothSelectionView.$el);
            this.$('.second-select-label').append(this.secondSelectionView.$el);

            this.firstSelectionView.render();
            this.bothSelectionView.render();
            this.secondSelectionView.render();

            this.mapView.setElement(this.$('.location-comparison-map')).render();

            this.$('.location-comparison-show-more').click(_.bind(function () {
                _.each(this.comparisons, function (comparison) {
                    this.fetchDocuments(comparison.model, comparison.collection);
                }, this);
                this.toggleLoading();
            }, this));

            this.addLayers();

            this.toggleLoading();
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

        createAddListeners: function (comparisons) {
            _.each(comparisons, function (comparison) {
                this.listenTo(comparison.collection, 'add', function (model) {
                    const location = _.findWhere(model.get('locations'), {displayName: comparison.model.get('field')});
                    if (location) {
                        const title = model.get('title');
                        const popover = this.popoverTemplate({
                            title: title,
                            i18n: i18n,
                            summary: addLinksToSummary(model.get('summary')),
                            cidForClickRouting: model.cid
                        });
                        const icon = this.mapView.getIcon('hp-record', 'white', comparison.color);
                        const marker = this.mapView.getMarker(location.latitude, location.longitude, icon, title, popover);
                        model.set('marker', marker);
                        comparison.layer.addLayer(marker);
                    }
                });
            }, this);
        },

        createSyncListeners: function (comparisons) {
            _.each(comparisons, function (comparison) {
                this.listenTo(comparison.collection, 'sync', function () {
                    const allMarkers = _.chain(this.comparisons).pluck('layer').invoke('getLayers').flatten().value();
                    if (!_.isEmpty(allMarkers) && !this.collectionsFetching()) {
                        this.mapView.loaded(allMarkers);
                    }
                    this.toggleLoading()
                });
            }, this)
        },

        createModelListeners: function (comparisons) {
            _.each(comparisons, function (comparison) {
                this.listenTo(comparison.model, 'change:field', _.bind(this.reloadMarkers, this, comparison));
            }, this)
        },

        addLayers: function () {
            _.each(this.comparisons, function (comparison) {
                this.mapView.addLayer(comparison.layer, comparison.name)
            }, this);
        },

        reloadMarkers: function (comparison) {
            this.clearMarkers(comparison.collection, comparison.layer);
            this.fetchDocuments(comparison.model, comparison.collection);
            this.toggleLoading();
        },

        clearMarkers: function (collection, layer) {
            collection.reset();
            layer.clearLayers();
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

        getFetchOptions: function (queryModel, selectedField, length) {
            const locationField = _.findWhere(this.locationFields, {displayName: selectedField});

            const latitudeFieldsInfo = configuration().fieldsInfo[locationField.latitudeField];
            const longitudeFieldsInfo = configuration().fieldsInfo[locationField.longitudeField];

            const latitudesFieldsString = latitudeFieldsInfo.names.join(':');
            const longitudeFieldsString = longitudeFieldsInfo.names.join(':');

            const exists = 'EXISTS{}:' + latitudesFieldsString + ' AND EXISTS{}:' + longitudeFieldsString;

            const newFieldText = queryModel.get('fieldText') ? queryModel.get('fieldText') + ' AND ' + exists : exists;

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

        fetchDocuments: function (queryModel, collection) {
            if (collection.length !== collection.totalResults) {
                const selectedField = queryModel.get('field');

                const options = this.getFetchOptions(queryModel, selectedField, collection.length);

                collection.fetch(options)
            }
        }
    });
});
