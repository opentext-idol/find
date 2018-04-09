/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'find/app/configuration',
    'find/app/page/search/results/map-view',
    'find/app/page/search/results/add-links-to-summary',
    'text!find/templates/app/page/search/document/location-tab.html',
    'text!find/templates/app/page/search/results/map-popover.html',
    'i18n!find/nls/bundle',
    'find/app/vent'
], function(Backbone, _, configuration, MapView, addLinksToSummary, templateString, popoverTemplate, i18n, vent) {

    'use strict';

    return Backbone.View.extend({
        map: null,
        template: _.template(templateString),
        popoverTemplate: _.template(popoverTemplate),

        initialize: function() {
            this.listenTo(vent, 'vent:resize', function() {
                if (this.map) {
                    this.map.invalidateSize();
                }
            });
            this.locationFields = configuration().map.locationFields;
            this.mapResultsView = new MapView({addControl: false});
        },

        render: function() {
            const locationsMap = this.model.get('locations');

            this.$el.html(this.template({
                i18n: i18n
            }));

            this.mapResultsView.setElement(this.$('.location-tab-map').get(0)).render();

            const markers = _.flatten(_.map(locationsMap, function(locations) {
                return _.map(locations, function(location){
                    const longitude = location.longitude;
                    const latitude = location.latitude;

                    const popover = this.popoverTemplate({
                        i18n: i18n,
                        title: location.displayName,
                        summary: addLinksToSummary(this.model.get('summary')),
                        cidForClickRouting: null
                    });

                    return this.mapResultsView.getMarker(latitude, longitude, this.getIcon(location.displayName), location.displayName, popover);
                }, this)
            }, this))

            this.mapResultsView.addMarkers(markers, false);

            const areasMap = this.model.get('areas');

            const areaLayers = _.flatten(_.map(areasMap, function(areas) {
                return _.map(areas, function(location){
                    const popover = this.popoverTemplate({
                        i18n: i18n,
                        title: location.displayName,
                        summary: addLinksToSummary(this.model.get('summary')),
                        cidForClickRouting: null
                    });

                    const locationField = _.findWhere(this.locationFields, {displayName: location.displayName});

                    return this.mapResultsView.getAreaLayer(location.polygon, locationField.markerColor, popover);
                }, this)
            }, this))

            if (areaLayers.length) {
                this.mapResultsView.addShapeLayers(areaLayers, false);
            }

            this.mapResultsView.fitMapToMarkerBounds();
        },

        getIcon: function(displayName) {
            const locationField = _.findWhere(this.locationFields, {displayName: displayName});
            return this.mapResultsView.getIcon(locationField.iconName, locationField.iconColor, locationField.markerColor);
        }
    });

});
