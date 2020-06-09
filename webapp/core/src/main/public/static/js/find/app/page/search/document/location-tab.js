/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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
                    const popover = this.popoverTemplate({
                        i18n: i18n,
                        title: location.displayName,
                        summary: addLinksToSummary(this.model.get('summary')),
                        cidForClickRouting: null
                    });

                    if (location.polygon) {
                        const locationField = _.findWhere(this.locationFields, {displayName: location.displayName});
                        return this.mapResultsView.getAreaLayer(location.polygon, locationField.markerColor, location.displayName, popover);
                    }

                    return this.mapResultsView.getMarker(location.latitude, location.longitude, this.getIcon(location.displayName), location.displayName, popover);
                }, this)
            }, this))

            this.mapResultsView.addMarkers(markers, false);

            this.mapResultsView.fitMapToMarkerBounds();
        },

        getIcon: function(displayName) {
            const locationField = _.findWhere(this.locationFields, {displayName: displayName});
            return this.mapResultsView.getIcon(locationField.iconName, locationField.iconColor, locationField.markerColor);
        }
    });

});
