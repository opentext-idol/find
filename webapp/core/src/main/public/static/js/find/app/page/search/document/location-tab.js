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
            var locationsMap = this.model.get('locations');

            this.$el.html(this.template({
                i18n: i18n
            }));

            this.mapResultsView.setElement(this.$('.location-tab-map').get(0)).render();

            var markers = _.flatten(_.map(locationsMap, function(locations){
                return _.map(locations, function(location){
                    var longitude = location.longitude;
                    var latitude = location.latitude;

                    var popover = this.popoverTemplate({
                        i18n: i18n,
                        title: location.displayName,
                        summary: addLinksToSummary(this.model.get('summary')),
                        cidForClickRouting: null
                    });

                    return this.mapResultsView.getMarker(latitude, longitude, this.getIcon(location.displayName), location.displayName, popover);
                }, this)
            }, this))

            this.mapResultsView.addMarkers(markers, false);
            this.mapResultsView.fitMapToMarkerBounds();
        },

        getIcon: function(displayName) {
            var locationField = _.findWhere(this.locationFields, {displayName: displayName});
            return this.mapResultsView.getIcon(locationField.iconName, locationField.iconColor, locationField.markerColor);
        }
    });

});
