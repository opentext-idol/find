/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    './saved-search-widget',
    'find/app/configuration',
    'find/app/page/search/results/map-view',
    'find/app/model/documents-collection',
    'i18n!find/nls/bundle',
], function (_, $, SavedSearchWidget, configuration, MapView, DocumentsCollection, i18n) {
    'use strict';

    return SavedSearchWidget.extend({
        viewType: 'map',

        initialize: function (options) {
            SavedSearchWidget.prototype.initialize.apply(this, arguments);

            this.locationFieldPairs = this.widgetSettings.locationFieldPairs;
            this.maxResults = this.widgetSettings.maxResults || 1000;
            this.clusterMarkers = this.widgetSettings.clusterMarkers || false;

            this.documentsCollection = new DocumentsCollection();

            this.mapView = new MapView({
                addControl: false,
                centerCoordinates: this.widgetSettings.centerCoordinates,
                initialZoom: this.widgetSettings.zoomLevel,
                removeZoomControl: true,
                disableInteraction: true
            });
        },

        render: function () {
            SavedSearchWidget.prototype.render.apply(this);
            this.mapView.setElement(this.$content).render();
            this.hasRendered = true;
        },

        getData: function () {
            if (!this.hasRendered) {
                return $.when();
            }

            const config = configuration();

            this.mapView.clearMarkers();
            const locationFields = config.map.locationFields.filter(function (locationField) {
                return _.contains(this.locationFieldPairs, locationField.displayName);
            }, this);

            if (_.isEmpty(locationFields)) {
                return $.when();
            }

            const fieldText = locationFields.map(function (locationField) {
                //noinspection JSUnresolvedVariable
                return '(EXISTS{}:' + config.fieldsInfo[locationField.latitudeField].names.join(':') +
                    ' AND EXISTS{}:' + config.fieldsInfo[locationField.longitudeField].names.join(':') + ')';
            }).join(' OR ');

            const newFieldText = this.queryModel.get('fieldText')
                ? '(' + this.queryModel.get('fieldText') + ') AND (' + fieldText + ')'
                : fieldText;

            return this.fetchDocumentCollection(newFieldText);
        },

        fetchDocumentCollection: function (newFieldText) {
            return this.documentsCollection.fetch({
                data: {
                    text: this.queryModel.get('queryText'),
                    max_results: this.maxResults,
                    indexes: this.queryModel.get('indexes'),
                    field_text: newFieldText,
                    min_date: this.queryModel.getIsoDate('minDate'),
                    max_date: this.queryModel.getIsoDate('maxDate'),
                    sort: 'relevance',
                    summary: 'context',
                    queryType: 'MODIFIED'
                },
                reset: false
            }).done(function () {
                const markers = {};

                this.documentsCollection.each(function (model) {
                    const locations = model.get('locations');
                    Object.keys(locations).forEach(function (locationName) {
                        locations[locationName].forEach(function (location) {
                            const longitude = location.longitude;
                            const latitude = location.latitude;
                            const title = i18n['search.resultsView.map.field'] + ': ' + locationName + '\n' + i18n['search.resultsView.map.title'] + ': ' + model.get('title');
                            const icon = this.mapView.getIcon(location.iconName, location.iconColor, location.markerColor);
                            const marker = this.mapView.getMarker(latitude, longitude, icon, title);

                            if (markers[location.displayName]) {
                                markers[location.displayName].push(marker);
                            } else {
                                markers[location.displayName] = [marker];
                            }
                        }, this);
                    }, this);
                }.bind(this));

                if (!_.isEmpty(markers)) {
                    const clusterLayer = this.clusterMarkers ? this.mapView.addClusterLayer() : null;

                    Object.keys(markers).forEach(function (markerName) {
                        this.mapView.addMarkers(markers[markerName], {
                            clusterLayer: clusterLayer
                        });
                    }, this);

                    this.mapView.fitMapToMarkerBounds();
                }
            }.bind(this));
        },

        exportData: function () {
            return this.mapView.exportData().then(function (data) {
                return {
                    data: data,
                    type: 'map'
                }
            });
        }
    });
});
