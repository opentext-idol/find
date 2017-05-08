/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'find/app/configuration',
    'find/app/vent',
    'leaflet',
    'Leaflet.awesome-markers',
    'leaflet.markercluster',
    'leaflet.markercluster.layersupport',
    'html2canvas'
], function(_, $, Backbone, configuration, vent, leaflet) {
    'use strict';

    const INITIAL_ZOOM = 3;

    const leafletMarkerColorMap = {
        'green': '#70ad25',
        'orange': '#f0932f',
        'red': '#d33d2a',
        'blue': '#37a8da'
    };

    function leftPadHex(str) {
        return str.length < 2
            ? '0' + str
            : str;
    }

    function leftPadMatch(match) {
        return leftPadHex(Number(match[1]).toString(16))
            + leftPadHex(Number(match[2]).toString(16))
            + leftPadHex(Number(match[3]).toString(16));
    }

    function hexColor(str) {
        let match;
        if(match = /rgba\((\d+),\s*(\d+),\s*(\d+),\s*([0-9.]+)\)/.exec(str)) {
            return '#' + leftPadMatch(match);
        } else if(match = /rgb\((\d+),\s*(\d+),\s*(\d+)\)/.exec(str)) {
            return '#' + leftPadMatch(match);
        } else {
            return str;
        }
    }

    return Backbone.View.extend({
        initialize: function(options) {
            this.addControl = options.addControl || false;

            this.centerCoordinates = options.centerCoordinates;
            this.initialZoom = options.initialZoom;
            this.removeZoomControl = options.removeZoomControl;
            this.disableInteraction = options.disableInteraction || false;

            this.layers = [];

            this.listenTo(vent, 'vent:resize', function() {
                if(this.map) {
                    this.map.invalidateSize();
                }
            });
        },

        render: function() {
            this.removeMap();
            const map = this.map = leaflet.map(this.$el.get(0), {
                attributionControl: false,
                minZoom: 1, // Furthest you can zoom out (smaller is further)
                maxZoom: 18,// Map does not display tiles above zoom level 18 (2016-07-06)
                worldCopyJump: true,
                zoomControl: !this.removeZoomControl,
                keyboard: !this.disableInteraction,
                dragging: !this.disableInteraction,
                scrollWheelZoom: !this.disableInteraction,
                tap: !this.disableInteraction,
                touchZoom: !this.disableInteraction
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

        mapRendered: function() {
            return !!this.map;
        },

        addClusterLayer: function(name, options) {
            const clusterLayer = leaflet.markerClusterGroup.layerSupport(_.defaults({
                zoomToBoundsOnClick: !this.disableInteraction,
                showCoverageOnHover: !this.disableInteraction
            }, options));
            this.addLayer(clusterLayer, name);

            return clusterLayer;
        },

        addGroupingLayer: function(name) {
            const layer = leaflet.layerGroup();
            this.addLayer(layer, name);
            return layer;
        },

        addMarkers: function(markers, options) {
            let layer;
            if(options.clusterLayer) {
                layer = leaflet.layerGroup(markers);
                options.clusterLayer.checkIn(layer);
            } else {
                layer = leaflet.featureGroup(markers);
            }

            if(options.groupingLayer) {
                options.groupingLayer.addLayer(layer);
            }

            this.addLayer(layer, options.name);
        },

        addLayer: function(layer, name) {
            this.map.addLayer(layer);
            this.layers.push(layer);
            if(this.control && name) {
                this.control.addOverlay(layer, name);
            }
        },

        fitMapToMarkerBounds: function() {
            const layers = this.layers.filter(function(layer) {
                return layer.getBounds;
            });
            const bounds = _.first(layers).getBounds();
            _.rest(layers).forEach(function(layer) {
                bounds.extend(layer.getBounds());
            });
            this.map.fitBounds(bounds);
        },

        getMarker: function(latitude, longitude, icon, title, popover) {
            if(popover) {
                return leaflet.marker([latitude, longitude], {icon: icon, title: title})
                    .bindPopup(popover);
            } else {
                return leaflet.marker([latitude, longitude], {icon: icon, title: title});
            }
        },

        getIcon: function(iconName, iconColor, markerColor) {
            return leaflet.AwesomeMarkers.icon({
                icon: iconName || 'hp-record',
                iconColor: iconColor || 'white',
                markerColor: markerColor || 'blue',
                prefix: 'hp',
                extraClasses: 'hp-icon'
            });
        },

        getDivIconCreateFunction: function(className) {
            return function(cluster) {
                return new leaflet.DivIcon({
                    html: '<div><span>' + cluster.getChildCount() + '</span></div>',
                    className: 'marker-cluster ' + className,
                    iconSize: new leaflet.Point(40, 40)
                });
            }
        },

        clearMarkers: function() {
            this.layers.forEach(function(layer) {
                layer.clearLayers();
            });
            this.layers = [];
        },

        remove: function() {
            this.removeMap();
            Backbone.View.prototype.remove.call(this);
        },

        removeMap: function() {
            if(this.map) {
                this.map.remove();
            }
        },

        exportData: function() {
            const deferred = $.Deferred();

            const map = this.map;
            const mapSize = map.getSize();
            const $mapEl = $(map.getContainer());
            const markers = [];

            map.eachLayer(function(layer) {
                if(layer instanceof leaflet.Marker) {
                    const pos = map.latLngToContainerPoint(layer.getLatLng());

                    const isCluster = layer.getChildCount;

                    const xFraction = pos.x / mapSize.x;
                    const yFraction = pos.y / mapSize.y;
                    const tolerance = 0.001;

                    if(xFraction > -tolerance && xFraction < 1 + tolerance && yFraction > -tolerance && yFraction < 1 + tolerance) {
                        let fontColor = '#000000',
                            color = '#37a8da',
                            match,
                            fade = false,
                            text = '';

                        const $iconEl = $(layer._icon);
                        if(isCluster) {
                            color = hexColor($iconEl.css('background-color'));
                            fontColor = hexColor($iconEl.children('div').css('color'));
                            fade = +$iconEl.css('opacity') < 1;
                            text = layer.getChildCount();
                        } else if(match = /awesome-marker-icon-(\w+)/.exec(layer._icon.classList)) {
                            if(leafletMarkerColorMap.hasOwnProperty(match[1])) {
                                color = leafletMarkerColorMap[match[1]]
                            }

                            if(layer.options.title) {
                                text = layer.options.title
                            } else {
                                const popup = layer.getPopup();
                                if(popup && popup._content) {
                                    text = $(popup._content).find('.map-popup-title').text()
                                }
                            }
                        }

                        const marker = {
                            x: xFraction,
                            y: yFraction,
                            text: text,
                            cluster: !!isCluster,
                            color: color,
                            fontColor: fontColor,
                            fade: fade,
                            z: +$iconEl.css('z-index')
                        };

                        markers.push(marker)
                    }
                }
            });

            const $objs = $mapEl.find('.leaflet-objects-pane, .leaflet-marker-pane, .leaflet-shadow-pane')
                .addClass('hide');

            html2canvas($mapEl, {
                // This seems to avoid issues with IE11 only rendering a small portion of the map the size of the window
                // If width and height are undefined, Firefox sometimes renders black areas.
                // If width and height are equal to the $mapEl.width()/height(), then Chrome has the same problem as IE11.
                width: $(document).width(),
                height: $(document).height(),
                proxy: 'api/public/map/proxy',
                useCORS: true,
                onrendered: function(canvas) {
                    $objs.removeClass('hide');

                    deferred.resolve({
                        // ask for lossless PNG image
                        image: canvas.toDataURL('image/png'),
                        markers: markers.sort(function(a, b) {
                            return a.z - b.z;
                        }).map(function(a) {
                            return _.omit(a, 'z')
                        })
                    });
                }
            });

            return deferred.promise();
        }
    });
});
