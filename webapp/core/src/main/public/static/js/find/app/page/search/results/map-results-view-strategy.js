/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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
    'underscore',
    'jquery',
    'backbone',
    'find/app/configuration',
    'find/app/page/search/results/add-links-to-summary',
    'find/app/page/search/results/map-view',
    'i18n!find/nls/bundle',
    'leaflet'
], function(_, $, Backbone, configuration, addLinksToSummary, MapView, i18n, leaflet) {
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
        const disableAutoZoom = options.disableAutoZoom;

        const mapView = new MapView(options.mapViewOptions);
        const parentLayerModel = new Backbone.Model();
        const polygonParentLayerModel = new Backbone.Model();
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
                        const marker = location.polygon
                            ? mapView.getAreaLayer(location.polygon, color || location.markerColor, title, popover)
                            : mapView.getMarker(location.latitude, location.longitude, icon, title, popover);

                        if(markers[location.displayName]) {
                            markers[location.displayName].push(marker);
                        } else {
                            markers[location.displayName] = [marker];
                        }
                    });
                });
            },

            createSyncListeners: function(listenTo, callback) {
                resultSets.forEach(function(resultSet) {
                    listenTo(resultSet.collection, 'sync', function() {
                        this.addMarkersToMap(resultSet.markers, resultSet.clusterLayer, true);

                        if(callback) {
                            callback();
                        }
                    }.bind(this));
                }, this)
            },

            addMarkersToMap: function(markerMap, clusterLayer, createParentLayers) {
                if(!_.isEmpty(markerMap)) {
                    _.each(markerMap, function(markers, markerName) {
                        // We put the polygons on their own parent layers so their visibility can be toggled separately.
                        const split = _.partition(markers, function(marker){
                            return marker instanceof leaflet.Polygon;
                        })

                        addMarkers(split[0], polygonParentLayerModel, i18n['search.resultsView.map.areas'](markerName))
                        addMarkers(split[1], parentLayerModel, i18n['search.resultsView.map.points'](markerName))

                        function addMarkers(markers, parentLayerModel, markerName){
                            if(!markers.length) {
                                return;
                            }

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
                        }
                    });
                    disableAutoZoom || mapView.fitMapToMarkerBounds();
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
                    polygonParentLayerModel.clear();
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
                        return locationField.geoindexField ? 'EXISTS{}:' + config.fieldsInfo[locationField.geoindexField].names.join(':') :
                            '(EXISTS{}:' + config.fieldsInfo[locationField.latitudeField].names.join(':') +
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
