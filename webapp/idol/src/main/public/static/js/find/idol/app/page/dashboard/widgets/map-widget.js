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
    'find/app/model/documents-collection'
], function(_, $, SavedSearchWidget, configuration, MapView, DocumentsCollection) {
    'use strict';

    return SavedSearchWidget.extend({
        viewType: 'map',

        initialize: function(options) {
            SavedSearchWidget.prototype.initialize.apply(this, arguments);
            this.markers = [];
            this.locationFieldPair = options.widgetSettings.locationFieldPair;
            this.maxResults = options.widgetSettings.maxResults || 1000;
            this.documentsCollection = new DocumentsCollection();
            this.clusterMarkers = options.widgetSettings.clusterMarkers || false;

            this.mapView = new MapView({
                addControl: false,
                centerCoordinates: options.widgetSettings.centerCoordinates,
                initialZoom: options.widgetSettings.zoomLevel,
                removeZoomControl: true,
                disableInteraction: true
            });
        },

        render: function() {
            SavedSearchWidget.prototype.render.apply(this);
            this.mapView.setElement(this.$content).render();
            this.hasRendered = true;
        },

        getIcon: function() {
            const locationField = _.findWhere(configuration().map.locationFields, {displayName: this.locationFieldPair});
            return this.mapView.getIcon(locationField.iconName, locationField.iconColor, locationField.markerColor);
        },

        postInitialize: function(){
            return this.getData();
        },

        getData: function() {
            if(!this.hasRendered) {
                return $.when();
            }

            this.markers = [];
            this.mapView.clearMarkers(this.clusterMarkers);
            const locationField = _.findWhere(configuration().map.locationFields, {displayName: this.locationFieldPair});

            const latitudeFieldsInfo = configuration().fieldsInfo[locationField.latitudeField];
            const longitudeFieldsInfo = configuration().fieldsInfo[locationField.longitudeField];

            const latitudesFieldsString = latitudeFieldsInfo.names.join(':');
            const longitudeFieldsString = longitudeFieldsInfo.names.join(':');

            const exists = 'EXISTS{}:' + latitudesFieldsString + ' AND EXISTS{}:' + longitudeFieldsString;

            const newFieldText = this.queryModel.get('fieldText') ? this.queryModel.get('fieldText') + ' AND ' + exists : exists;

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
            }).done(function() {
                this.documentsCollection.each(function(model) {
                    const locations = model.get('locations');
                    const location = _.findWhere(locations, {displayName: this.locationFieldPair});
                    if(location) {
                        const longitude = location.longitude;
                        const latitude = location.latitude;
                        const title = model.get('title');
                        const marker = this.mapView.getMarker(latitude, longitude, this.getIcon(), title);
                        this.markers.push(marker);
                    }
                }.bind(this));
                if(!_.isEmpty(this.markers)) {
                    this.mapView.addMarkers(this.markers, this.clusterMarkers);
                }
            }.bind(this));
        },

        exportPPTData: function(){
            return this.mapView.exportPPTData().then(function(data){
                return {
                    data: data,
                    type: 'map'
                }
            });
        }
    });
});
