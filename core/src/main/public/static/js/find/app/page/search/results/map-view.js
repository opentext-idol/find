define([
    'backbone',
    'underscore',
    'jquery',
    'find/app/configuration',
    'find/app/vent',
    'leaflet',
    'Leaflet.awesome-markers',
    'leaflet.markercluster'

], function (Backbone, _, $, configuration, vent, leaflet) {

    'use strict';
    var INITIAL_ZOOM = 3;

    return Backbone.View.extend({
        initialize: function (options) {
            this.addControl = options.addControl || false;

            this.clusterMarkers = leaflet.markerClusterGroup();
            this.markerLayerGroup = leaflet.featureGroup();
            this.markers = [];

            this.listenTo(vent, 'vent:resize', function () {
                if (this.map) {
                    this.map.invalidateSize();
                }
            });            
        },

        render: function () {
            this.removeMap();
            var map = this.map = leaflet.map(this.$el.get(0), {
                attributionControl: false,
                minZoom: 1, // Furthest you can zoom out (smaller is further)
                maxZoom: 18,// Map does not display tiles above zoom level 18 (2016-07-06)
                worldCopyJump: true
            });

            leaflet
                .tileLayer(configuration().map.tileUrlTemplate)
                .addTo(map);

            var attributionText = configuration().map.attribution;

            if (this.addControl) {
                this.control = leaflet.control.layers().addTo(map);
            }

            if (attributionText) {
                leaflet.control.attribution({prefix: false})
                    .addAttribution(attributionText)
                    .addTo(map);
            }
            
            var initialLatitude = configuration().map.initialLocation.latitude;
            var initialLongitude = configuration().map.initialLocation.longitude;

            map.setView([initialLatitude, initialLongitude], INITIAL_ZOOM);
        },

        addMarkers: function(markers, cluster) {
            this.markers = markers;
            if (cluster) {
                this.clusterMarkers.addLayers(markers);
                this.map.addLayer(this.clusterMarkers)
            }
            else {
                this.markerLayerGroup = new leaflet.featureGroup(markers);
                this.map.addLayer(this.markerLayerGroup);
            }
        },

        createLayer: function(options) {
            // This can be changed in the future to create a cluster or normal group if needed.
            return new leaflet.markerClusterGroup(options);
        },
        
        addLayer: function(layer, name) {
            this.map.addLayer(layer);
            if (this.control) {
                this.control.addOverlay(layer, name);
            }
        },        
        
        loaded: function(markers) {
            this.map.fitBounds(new leaflet.featureGroup(_.isEmpty(markers) ? this.markers : markers))
        },
        
        getMarker: function(latitude, longitude, icon, title, popover) {
            return leaflet.marker([latitude, longitude], {icon: icon, title: title})
                .bindPopup(popover);            
        },
        
        getIcon: function (iconName, iconColor, markerColor) {
            return leaflet.AwesomeMarkers.icon({
                icon: iconName || 'compass',
                iconColor: iconColor || 'white',
                markerColor: markerColor || 'blue',
                prefix: 'hp',
                extraClasses: 'hp-icon'
            });
        },

        getDivIconCreateFunction: function(className) {
            return function (cluster) {
                return new leaflet.DivIcon({
                    html: '<div><span>' + cluster.getChildCount() + '</span></div>',
                    className: 'marker-cluster ' + className,
                    iconSize: new leaflet.Point(40, 40)
                });
            }
        },

        clearMarkers: function (cluster) {
            if (cluster) {
                this.clusterMarkers.clearLayers();
            } else {
                this.markerLayerGroup.clearLayers();
            }
            this.markers = [];
        },

        remove: function () {
            this.removeMap();
            Backbone.View.prototype.remove.call(this);
        },

        removeMap: function () {
            if (this.map) {
                this.map.remove();
            }
        }
    });
});
