/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'leaflet',
    'find/app/configuration',
    'text!find/templates/app/page/search/document/location-tab.html',
    'i18n!find/nls/bundle',
    'find/app/vent'
], function(Backbone, _, leaflet, configuration, templateString, i18n, vent) {

    'use strict';

    var INITIAL_ZOOM = 13;

    return Backbone.View.extend({
        map: null,
        template: _.template(templateString),

        initialize: function() {
            this.listenTo(vent, 'vent:resize', function() {
                if (this.map) {
                    this.map.invalidateSize();
                }
            });
        },

        render: function() {
            var latitude = this.model.get('latitude');
            var longitude = this.model.get('longitude');

            this.$el.html(this.template({
                i18n: i18n,
                latitude: latitude,
                longitude: longitude
            }));

            this.removeMap();
            var map = this.map = leaflet.map(this.$('.location-tab-map').get(0), {attributionControl: false});

            function setInitialView() {
                map.setView([latitude, longitude], INITIAL_ZOOM);
            }

            setInitialView();

            leaflet
                .tileLayer(configuration().map.tileUrlTemplate)
                .addTo(map);

            // Create the icon by hand rather than using the default because the leaflet.Icon.Default.imagePath method
            // does not work after JS concatenation
            var icon = leaflet.icon(_.defaults({
                iconUrl: '../static-' + configuration().commit + '/bower_components/leaflet/dist/images/marker-icon.png'
            }, leaflet.Icon.Default.prototype.options));

            leaflet
                .marker([latitude, longitude], {icon: icon})
                .addTo(map)
                .on('click', setInitialView);

            var attributionText = configuration().map.attribution;

            if (attributionText) {
                leaflet.control.attribution({prefix: false})
                    .addAttribution(attributionText)
                    .addTo(map);
            }
        },

        remove: function() {
            this.removeMap();
            Backbone.View.prototype.remove.call(this);
        },

        removeMap: function() {
            if (this.map) {
                this.map.remove();
            }
        }
    });

});
