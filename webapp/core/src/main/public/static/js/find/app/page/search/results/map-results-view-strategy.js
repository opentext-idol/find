/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'find/app/configuration',
    'find/app/page/search/results/add-links-to-summary',
    'find/app/page/search/results/map-view',
    'i18n!find/nls/bundle'
], function(_, $, Backbone, configuration, addLinksToSummary, MapView, i18n) {
    'use strict';

    return function(options) {
        const allowIncrement = options.allowIncrement;
        const resultsStep = options.resultsStep;
        const clusterMarkers = options.clusterMarkers;
        const locationFields = options.locationFields;
        const popoverTemplate = options.popoverTemplate;
        const toggleLoading = options.toggleLoading;
        const resultSets = options.resultSets;
        const errorCallback = options.errorCallback;

        const mapView = new MapView(options.mapViewOptions);
        const parentLayerModel = new Backbone.Model();
        const errorModel = options.errorModel || new Backbone.Model();

        return {
            mapView: mapView,

            createAddListeners: function(listenTo) {
                resultSets.forEach(function(resultSet) {
                    listenTo(resultSet.collection, 'add', function(model) {
                        return this.getMarkersFromDocumentModel(model, resultSet.markers, resultSet.color);
                    }.bind(this));
                }, this);
            },

            listenForErrors: function(listenTo) {
                resultSets.forEach(function(resultSet) {
                    listenTo(resultSet.collection, 'error', function(collection, xhr) {
                        if(xhr.status !== 0) {
                            errorModel.set({
                                hasError: true,
                                responseJSON: xhr.responseJSON
                            });
                        }
                    }.bind(this));
                }, this);
                listenTo(errorModel, 'change:hasError', function() {
                    errorCallback(errorModel.attributes);
                });
            },

            getMarkersFromDocumentModel: function(model, markers, color) {
                const locations = model.get('locations');
                _.each(locations, function(locationValues, locationName) {
                    locationValues.forEach(function(location) {
                        const longitude = location.longitude;
                        const latitude = location.latitude;
                        const title = model.get('title');
                        const titleHover = i18n['search.resultsView.map.field'] + ': ' + locationName;

                        const popover = popoverTemplate
                            ? popoverTemplate({
                                title: title,
                                titleHover: titleHover,
                                i18n: i18n,
                                summary: addLinksToSummary(model.get('summary')),
                                cidForClickRouting: model.cid
                            })
                            : null;
                        const icon = mapView.getIcon(location.iconName, location.iconColor, color || location.markerColor);
                        const marker = mapView.getMarker(latitude, longitude, icon, title, popover);

                        if(markers[location.displayName]) {
                            markers[location.displayName].push(marker);
                        } else {
                            markers[location.displayName] = [marker];
                        }
                    });
                });
            },

            createSyncListeners: function(listenTo, callback) {
                const createParentLayers = resultSets.length > 1;
                resultSets.forEach(function(resultSet) {
                    listenTo(resultSet.collection, 'sync', function() {
                        this.addMarkersToMap(resultSet.markers, resultSet.clusterLayer, createParentLayers);

                        if(callback) {
                            callback();
                        }
                    }.bind(this));
                }, this)
            },

            addMarkersToMap: function(markerMap, clusterLayer, createParentLayers) {
                if(!_.isEmpty(markerMap)) {
                    _.each(markerMap, function(markers, markerName) {
                        let parentLayer;
                        if(createParentLayers) {
                            parentLayer = parentLayerModel.get(markerName);
                            if(!parentLayer) {
                                parentLayer = mapView.addGroupingLayer(markerName);
                                parentLayerModel.set(markerName, parentLayer);
                            }
                        }
                        mapView.addMarkers(markers, {
                            clusterLayer: clusterLayer,
                            groupingLayer: parentLayer,
                            name: parentLayer
                                ? null
                                : markerName
                        });
                    });
                    mapView.fitMapToMarkerBounds();
                }
                toggleLoading();
            },

            reloadMarkers: function() {
                if(mapView.mapRendered()) {
                    mapView.clearMarkers();
                    resultSets.forEach(function(resultSet) {
                        resultSet.collection.reset();
                        resultSet.clusterLayer = clusterMarkers
                            ? mapView.addClusterLayer(resultSet.name, resultSet.layerOptions)
                            : null;
                        resultSet.markers = {};
                    });
                    parentLayerModel.clear();
                    return this.fetchDocuments();
                }

                return null;
            },

            collectionsFetching: function() {
                return _.chain(resultSets)
                    .pluck('collection')
                    .pluck('fetching')
                    .some()
                    .value();
            },

            collectionsFull: function() {
                return _.chain(resultSets)
                    .pluck('collection')
                    .reject(function(collection) {
                        return collection.length === collection.totalResults
                    })
                    .isEmpty()
                    .value();
            },

            getFetchOptions: function(queryModel, fieldText, length) {
                const newFieldText = queryModel.get('fieldText')
                    ? '(' + queryModel.get('fieldText') + ') AND (' + fieldText + ')'
                    : fieldText;

                return {
                    data: {
                        text: queryModel.get('queryText'),
                        start: allowIncrement
                            ? length + 1
                            : 1,
                        max_results: allowIncrement
                            ? length + resultsStep
                            : resultsStep,
                        indexes: queryModel.get('indexes'),
                        field_text: newFieldText,
                        min_date: queryModel.getIsoDate
                            ? queryModel.getIsoDate('minDate')
                            : null,
                        max_date: queryModel.getIsoDate
                            ? queryModel.getIsoDate('maxDate')
                            : null,
                        state_match_ids: queryModel.get('stateMatchIds'),
                        state_dont_match_ids: queryModel.get('stateDontMatchIds'),
                        sort: 'relevance',
                        summary: 'context',
                        queryType: 'MODIFIED'
                    },
                    remove: false,
                    reset: false
                };
            },

            fetchDocuments: function() {
                const config = configuration();

                const locationFieldsToRetrieve = config.map
                    .locationFields
                    .filter(function(locationField) {
                        return _.isEmpty(locationFields) || _.contains(locationFields, locationField.displayName);
                    }, this);

                if(!_.isEmpty(locationFieldsToRetrieve)) {
                    errorModel.set('hasError', false);

                    const fieldText = locationFieldsToRetrieve.map(function(locationField) {
                        return '(EXISTS{}:' + config.fieldsInfo[locationField.latitudeField].names.join(':') +
                            ' AND EXISTS{}:' + config.fieldsInfo[locationField.longitudeField].names.join(':') + ')';
                    }).join(' OR ');

                    const promises = resultSets
                        .map(function(resultSet) {
                            const options = this.getFetchOptions(resultSet.model, fieldText, resultSet.collection.length);
                            return resultSet.collection.fetch(options);
                        }, this);

                    toggleLoading();

                    return $.when.apply($, promises);
                }

                return null;
            }
        };
    }
});
