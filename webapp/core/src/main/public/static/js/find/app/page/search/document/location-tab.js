/*
 * Copyright 2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

const Backbone = require('backbone');
const _ = require('underscore');
const configuration = require('find/app/configuration');
const MapView = require('find/app/page/search/results/map-view');
const addLinksToSummary = require('find/app/page/search/results/add-links-to-summary');
const templateString = require('find/templates/app/page/search/document/location-tab.html');
const popoverTemplate = require('find/templates/app/page/search/results/map-popover.html');
const i18n = require('find/nls/bundle');
const vent = require('find/app/vent');

module.exports = Backbone.View.extend({
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

