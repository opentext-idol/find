define([
    'backbone',
    'jquery',
    'underscore',
    'leaflet',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filters/geography/geography-editor-view.html',
    'leaflet.draw.i18n'
], function (Backbone, $, _, leaflet, configuration, i18n, template) {
    'use strict';

    const INITIAL_ZOOM = 3;

    return Backbone.View.extend({
        template: _.template(template),
        className: 'full-height',

        events: {
        },

        initialize: function (options) {
            this.shapes = options.shapes;
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

            const drawnItems = this.drawnItems = leaflet.featureGroup().addTo(map);

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

            map.addControl(new leaflet.Control.Draw({
                edit: {
                    featureGroup: drawnItems,
                    poly: {
                        allowIntersection: false
                    }
                },
                draw: {
                    marker: false,
                    polyline: false,
                    rectangle: false,
                    circle: {
                        repeatMode: true
                    },
                    polygon: {
                        repeatMode: true,
                        allowIntersection: false,
                        showArea: true
                    }
                }
            }));

            map.on(leaflet.Draw.Event.CREATED, function (event) {
                var layer = event.layer;

                drawnItems.addLayer(layer);
            });

            if (this.shapes) {
                _.each(this.shapes, function(shape){
                    switch(shape.type) {
                        case 'circle':
                            const center = shape.center;
                            drawnItems.addLayer(leaflet.circle(leaflet.latLng(center[0], center[1]), shape.radius));
                            break;
                        case 'polygon':
                            const pts = _.map(shape.points, function(pt){
                                return leaflet.latLng(pt[0], pt[1]);
                            });
                            drawnItems.addLayer(leaflet.polygon(pts));
                            break;
                    }
                }, this);
            }
        },

        updateMapSize: function(){
            // This is called when the containing modal is shown (and therefore the size is available).
            if (this.map) {
                this.map.invalidateSize();

                // If we have shapes on the screen, resize the visible map area to cover them all.
                const layers = this.drawnItems.getLayers();
                if (layers.length) {
                    let bounds = layers[0].getBounds();
                    layers.slice(1).forEach(layer => bounds.extend(layer.getBounds()))
                    this.map.fitBounds(bounds);
                }
            }
        },

        remove: function() {
            this.removeMap();
            Backbone.View.prototype.remove.call(this);
        },

        getShapes: function() {
            var shapes = []
            if (this.drawnItems) {
                _.each(this.drawnItems.getLayers(), function(layer){
                    if (layer instanceof leaflet.Circle) {
                        const latLng = layer.getLatLng();
                        shapes.push({ type: 'circle', center: [latLng.lat, latLng.lng], radius: layer.getRadius() });
                    }
                    else if (layer instanceof leaflet.Polygon) {
                        shapes.push({ type: 'polygon', points: _.map(layer.getLatLngs()[0], function(latLng){
                            return [latLng.lat, latLng.lng]
                        }) });
                    }
                });
            }
            return shapes;
        },

        removeMap: function() {
            if(this.map) {
                this.map.remove();
                this.map = this.drawnItems = undefined;
            }
        }
    });
});
