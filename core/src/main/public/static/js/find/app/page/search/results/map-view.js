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
    var INITIAL_ZOOM = 12;

    return Backbone.View.extend({
        clusterMarkers: leaflet.markerClusterGroup(),
        markerLayerGroup: leaflet.featureGroup(),
        markers: [],

        initialize: function () {
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
                worldCopyJump: true
            });

            leaflet
                .tileLayer(configuration().map.tileUrlTemplate)
                .addTo(map);

            var attributionText = configuration().map.attribution;

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
        
        loaded: function() {
            this.map.fitBounds(new leaflet.featureGroup(this.markers))
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
