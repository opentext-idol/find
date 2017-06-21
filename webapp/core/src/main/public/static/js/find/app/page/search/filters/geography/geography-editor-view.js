define([
    'backbone',
    'jquery',
    'underscore',
    'leaflet',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filters/geography/geography-editor-view.html',
    'leaflet.draw'
], function (Backbone, $, _, leaflet, configuration, i18n, template) {
    'use strict';

    const INITIAL_ZOOM = 3;

    return Backbone.View.extend({
        template: _.template(template),
        className: 'full-height',

        events: {
        },

        initialize: function (options) {
            options.geography;
        },

        render: function () {
            this.removeMap();

            this.$el.html(this.template({
                i18n: i18n
            }));

            const map = this.map = leaflet.map(this.$el.get(0), {
                attributionControl: false,
                minZoom: 1, // Furthest you can zoom out (smaller is further)
                maxZoom: 18,// Map does not display tiles above zoom level 18 (2016-07-06)
                worldCopyJump: true,
                zoomControl: true,
                keyboard: true,
                dragging: true,
                scrollWheelZoom: true,
                tap: true,
                touchZoom: true
            });

            leaflet
                .tileLayer(configuration().map.tileUrlTemplate)
                .addTo(map);

            const attributionText = configuration().map.attribution;

            if(this.addControl) {
                this.control = leaflet.control.layers().addTo(map);
            }

            if(attributionText) {
                leaflet.control.attribution({prefix: false})
                    .addAttribution(attributionText)
                    .addTo(map);
            }

            const initialLatitude = this.centerCoordinates
                ? this.centerCoordinates.latitude
                : configuration().map.initialLocation.latitude;
            const initialLongitude = this.centerCoordinates
                ? this.centerCoordinates.longitude
                : configuration().map.initialLocation.longitude;

            map.setView([initialLatitude, initialLongitude], this.initialZoom
                ? this.initialZoom
                : INITIAL_ZOOM);
        },

        updateMapSize: function(){
            if (this.map) {
                this.map.invalidateSize();
            }
        },

        remove: function() {
            this.removeMap();
            Backbone.View.prototype.remove.call(this);
        },

        removeMap: function() {
            if(this.map) {
                this.map.remove();
            }
        }
    });
});
