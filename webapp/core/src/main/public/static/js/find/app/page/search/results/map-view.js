define([
    'backbone',
    'underscore',
    'jquery',
    'find/app/configuration',
    'find/app/vent',
    'leaflet',
    'Leaflet.awesome-markers',
    'leaflet.markercluster',
    'html2canvas'
], function (Backbone, _, $, configuration, vent, leaflet) {

    'use strict';
    var INITIAL_ZOOM = 3;

    var leafletMarkerColorMap = {
        'green': '#70ad25',
        'orange': '#f0932f',
        'red': '#d33d2a',
        'blue': '#37a8da'
    }

    return Backbone.View.extend({
        initialize: function (options) {
            this.addControl = options.addControl || false;

            this.centerCoordinates = options.centerCoordinates;
            this.initialZoom = options.initialZoom;
            this.removeZoomControl = options.removeZoomControl;
            this.disableInteraction = options.disableInteraction || false;

            this.clusterMarkers = leaflet.markerClusterGroup({
                zoomToBoundsOnClick: !this.disableInteraction,
                showCoverageOnHover: !this.disableInteraction
            });
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
                worldCopyJump: true,
                zoomControl: this.removeZoomControl ? false : true,
                keyboard: !this.disableInteraction,
                dragging: !this.disableInteraction,
                scrollWheelZoom: !this.disableInteraction,
                tap: !this.disableInteraction,
                touchZoom: !this.disableInteraction
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
            
            var initialLatitude = this.centerCoordinates ? this.centerCoordinates.latitude : configuration().map.initialLocation.latitude;
            var initialLongitude = this.centerCoordinates ? this.centerCoordinates.longitude : configuration().map.initialLocation.longitude;

            map.setView([initialLatitude, initialLongitude], this.initialZoom ? this.initialZoom : INITIAL_ZOOM);
        },

        addMarkers: function(markers, cluster) {
            this.markers = markers;
            if (cluster) {
                this.clusterMarkers.addLayers(markers);
                this.map.addLayer(this.clusterMarkers);
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
            if (popover) {
                return leaflet.marker([latitude, longitude], {icon: icon, title: title})
                    .bindPopup(popover);
            } else {
                return leaflet.marker([latitude, longitude], {icon: icon, title: title});
            }
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
        },

        exportPPTData: function() {
            var deferred = $.Deferred();

            var map = this.map,
                mapSize = map.getSize(),
                $mapEl = $(map.getContainer()),
                markers = [];

            function lPad(str) {
                return str.length < 2 ? '0' + str : str
            }

            function hexColor(str){
                var match;
                if (match = /rgba\((\d+),\s*(\d+),\s*(\d+),\s*([0-9.]+)\)/.exec(str)) {
                    return '#' + lPad(Number(match[1]).toString(16))
                        + lPad(Number(match[2]).toString(16))
                        + lPad(Number(match[3]).toString(16))
                }
                else if (match = /rgb\((\d+),\s*(\d+),\s*(\d+)\)/.exec(str)) {
                    return '#' + lPad(Number(match[1]).toString(16))
                        + lPad(Number(match[2]).toString(16))
                        + lPad(Number(match[3]).toString(16))
                }
                return str
            }

            map.eachLayer(function(layer){
                if (layer instanceof leaflet.Marker) {
                    var pos = map.latLngToContainerPoint(layer.getLatLng())

                    var isCluster = layer.getChildCount

                    var xFraction = pos.x / mapSize.x;
                    var yFraction = pos.y / mapSize.y;
                    var tolerance = 0.001;

                    if (xFraction > -tolerance && xFraction < 1 + tolerance && yFraction > -tolerance && yFraction < 1 + tolerance) {
                        var fontColor = '#000000',
                            color = '#37a8da',
                            match,
                            fade = false,
                            text = '';

                        var $iconEl = $(layer._icon);
                        if (isCluster) {
                            color = hexColor($iconEl.css('background-color'));
                            fontColor = hexColor($iconEl.children('div').css('color'))
                            fade = +$iconEl.css('opacity') < 1
                            text = layer.getChildCount();
                        } else if (match=/awesome-marker-icon-(\w+)/.exec(layer._icon.classList)) {
                            if (leafletMarkerColorMap.hasOwnProperty(match[1])) {
                                color = leafletMarkerColorMap[match[1]]
                            }

                            var popup = layer.getPopup();
                            if (popup && popup._content) {
                                text = $(popup._content).find('.map-popup-title').text()
                            }
                        }

                        var marker = {
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
            })

            var $objs = $mapEl.find('.leaflet-objects-pane').addClass('hide')

            html2canvas($mapEl, {
                // This seems to avoid issues with IE11 only rendering a small portion of the map the size of the window
                // If width and height are undefined, Firefox sometimes renders black areas.
                // If width and height are equal to the $mapEl.width()/height(), then Chrome has the same problem as IE11.
                width: $(document).width(),
                height: $(document).height(),
                proxy: 'api/public/map/proxy',
                useCORS: true,
                onrendered: function(canvas) {
                    $objs.removeClass('hide')

                    deferred.resolve({
                        // ask for lossless PNG image
                        image: canvas.toDataURL('image/png'),
                        markers: markers.sort(function(a, b){
                            return a.z - b.z;
                        }).map(function(a){
                            return _.omit(a, 'z')
                        })
                    });
                }
            });

            return deferred.promise();
        },

        exportPPT: function(title){
            this.exportPPTData().done(function(data){
                // We use a textarea for the title so we can have newlines, and a textarea for the image to work
                //   around a hard 524288 limit imposed by a WebKit bug (affects Chrome 55).
                // See https://bugs.webkit.org/show_bug.cgi?id=44883
                // We open in _self (despite the chance of having errors) since otherwise the popup blocker
                ///  will block it, since it's a javascript object which doesn't originate directly from a user event.
                var $form = $('<form class="hide" enctype="multipart/form-data" method="post" target="_self" action="api/bi/export/ppt/map"><textarea name="title"></textarea><textarea name="data"></textarea><input type="submit"></form>');
                $form[0].title.value = title

                $form[0].data.value = JSON.stringify(data)

                $form.appendTo(document.body).submit().remove()
            })
        }
    });
});
