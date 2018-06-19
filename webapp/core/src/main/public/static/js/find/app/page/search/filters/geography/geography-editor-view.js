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

    const colorMapping = {
        'within': ['#01a982', '#ff0000'],
        'intersect': ['#e4ff00', '#fa8800'],
        'contains': ['#0066ff', '#a106ba']
    }

    const intersectionTypes = _.keys(colorMapping);

    function shapeColor(shape) {
        const colorMap = shape && shape.spatial && colorMapping[shape.spatial];
        const color = (colorMap || colorMapping.within)[shape && shape.negated ? 1 : 0];
        return { color: color, fillColor: color };
    }

    function bindTooltipOnAdd(layer) {
        if (layer instanceof leaflet.Polygon || layer instanceof leaflet.Circle) {
            layer.bindTooltip(layerTooltip, {direction: 'top', sticky: true, offset: [0, -10]});
            // We have to call this explicitly, otherwise the tooltip doesn't update till the next time the user
            //  hovers out of the shape, then moves the mouse back onto it.
            layer.on('polygonSpatialChange negated', updateTooltipContent)
        }
    }

    function updateTooltipContent(layer) {
        const tooltip = layer.getTooltip();
        if (tooltip) {
            layer.setTooltipContent(layerTooltip);
        }
    }

    function layerTooltip(shape) {
        if (shape instanceof leaflet.Circle) {
            return shape.negated ? i18n['search.geography.shape.tooltip.circle.negated']
                : i18n['search.geography.shape.tooltip.circle']
        }

        if (shape instanceof leaflet.Polygon) {
            switch (shape.spatial) {
                case 'intersect':
                    return shape.negated ? i18n['search.geography.shape.tooltip.polygon.intersect.negated']
                        : i18n['search.geography.shape.tooltip.polygon.intersect']
                case 'contains':
                    return shape.negated ? i18n['search.geography.shape.tooltip.polygon.contain.negated']
                        : i18n['search.geography.shape.tooltip.polygon.contain']
            }

            return shape.negated ? i18n['search.geography.shape.tooltip.polygon.within.negated']
                : i18n['search.geography.shape.tooltip.polygon.within']
        }

        return '';
    }

    return Backbone.View.extend({
        template: _.template(template),
        className: 'full-height',

        events: {
        },

        initialize: function (options) {
            this.shapes = options.shapes;
            this.geospatialUnified = options.geospatialUnified;
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

            drawnItems.on('layeradd', function(evt){
                const layer = evt.layer;
                bindTooltipOnAdd(layer);
            }, this)

            drawnItems.on('layerremove', function(evt){
                const layer = evt.layer;
                if (layer instanceof leaflet.Polygon || layer instanceof leaflet.Circle ) {
                    layer.unbindTooltip();
                    layer.off('polygonSpatialChange negated', updateTooltipContent);
                }
            }, this)

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

            const drawControls = new leaflet.Control.Draw({
                edit: {
                    featureGroup: drawnItems,
                    edit: {
                        selectedPathOptions: { maintainColor: true }
                    },
                    poly: {
                        allowIntersection: false
                    },
                    polygonSpatial: this.geospatialUnified ? {
                        shapeOptions: {
                            colorFn: shapeColor,
                            intersectionTypes: intersectionTypes
                        }
                    } : false,
                    negate: {
                        shapeOptions: {
                            colorFn: shapeColor
                        }
                    }
                },
                draw: {
                    marker: false,
                    polyline: false,
                    rectangle: false,
                    circle: {
                        repeatMode: true,
                        shapeOptions: shapeColor()
                    },
                    polygon: {
                        repeatMode: true,
                        allowIntersection: false,
                        showArea: true,
                        shapeOptions: shapeColor()
                    }
                }
            });

            map.addControl(drawControls);

            {
                // Disable automatic revert, so people can hit manual revert if they like.
                drawControls._toolbars.edit.disable = function () {
                    if (!this.enabled()) {
                        return;
                    }

                    // This line is commented out from the original.
                    // this._activeMode.handler.revertLayers();

                    leaflet.Toolbar.prototype.disable.call(this);
                }

                // We need to make the 'cancel' button trigger an explicit revert now.
                drawControls._toolbars.edit.getActions = function(handler){
                    if (handler instanceof leaflet.EditToolbar.Negate || handler instanceof leaflet.EditToolbar.PolygonSpatial) {
                        return [];
                    }
                    return [{
                        title: leaflet.drawLocal.edit.toolbar.actions.save.title,
                        text: leaflet.drawLocal.edit.toolbar.actions.save.text,
                        callback: this._save,
                        context: this
                    }, {
                        title: leaflet.drawLocal.edit.toolbar.actions.cancel.title,
                        text: leaflet.drawLocal.edit.toolbar.actions.cancel.text,
                        context: this,
                        callback: function () {
                            if (this.enabled()) {
                                this._activeMode.handler.revertLayers();
                                this.disable();
                            }
                        }
                    }];
                }
            }

            map.on(leaflet.Draw.Event.CREATED, function (event) {
                drawnItems.addLayer(event.layer);
            });

            if (this.shapes) {
                _.each(this.shapes, function(shape){
                    const shapeOpts = { negated: shape.NOT, spatial: shape.spatial };
                    const colorOpts = shapeColor(shapeOpts);
                    let layer;

                    switch(shape.type) {
                        case 'circle':
                            const center = shape.center;
                            layer = leaflet.circle(leaflet.latLng(center[0], center[1]), shape.radius, colorOpts)
                            break;
                        case 'polygon':
                            const pts = _.map(shape.points, function(pt){
                                return leaflet.latLng(pt[0], pt[1]);
                            });
                            layer = leaflet.polygon(pts, colorOpts);
                            break;
                    }

                    if (layer) {
                        _.extend(layer, shapeOpts);
                        drawnItems.addLayer(layer);
                    }
                }, this);
            }

            // Add a delete-all button.
            map.addControl(new (leaflet.Control.extend({
                options: {
                    position: 'topleft'
                },
                onAdd: function (map) {
                    const container = this.clearAllBtn = leaflet.DomUtil.create('div', 'leaflet-bar leaflet-control leaflet-touch');
                    container.innerHTML = '<a title="'+_.escape(i18n['search.geography.deleteAll'])+'"><i class="hp-icon hp-trash text-danger"></i></a>';
                    leaflet.DomEvent.on(container, 'click', this.clearLayers, this)
                    leaflet.DomEvent.on(container, leaflet.Draggable.START.join(' '), leaflet.DomEvent.stopPropagation);
                    map.on(leaflet.Draw.Event.CREATED, this.updateStatus, this);
                    map.on(leaflet.Draw.Event.DELETED, this.updateStatus, this);
                    this.updateStatus();
                    return container;
                },
                clearLayers: function(evt){
                    // If the user was partway deleting shapes, pressing the 'Delete all' button must clear the delete
                    //   toolbar's internal list to prevent user-deleted shapes coming back when they press 'Cancel'.
                    const deleted = drawControls._toolbars.edit._modes.remove.handler._deletedLayers;
                    deleted && deleted.clearLayers();
                    drawnItems.clearLayers();
                    this.updateStatus();
                    leaflet.DomEvent.stopPropagation(evt);
                    leaflet.DomEvent.preventDefault(evt);
                },
                updateStatus: function(){
                    $('i', this.clearAllBtn).toggleClass('geography-btn-disabled', drawnItems.getLayers().length === 0);
                },
                onRemove: function(){
                    leaflet.DomEvent.removeListener(this.clearAllBtn, 'click', this.clearLayers, this);
                    leaflet.DomEvent.removeListener(this.clearAllBtn, leaflet.Draggable.START.join(' '), leaflet.DomEvent.stopPropagation);
                    map.off(leaflet.Draw.Event.CREATED, this.updateStatus, this);
                    map.off(leaflet.Draw.Event.DELETED, this.updateStatus, this);
                }
            })))
        },

        updateMapSize: function(){
            // This is called when the containing modal is shown (and therefore the size is available).
            if (this.map) {
                this.map.invalidateSize();

                // If we have shapes on the screen, resize the visible map area to cover them all.
                const layers = this.drawnItems.getLayers();
                if (layers.length) {
                    let bounds = layers[0].getBounds();
                    layers.slice(1).forEach(function(layer) { return bounds.extend(layer.getBounds()) })
                    this.map.fitBounds(bounds);
                }
            }
        },

        remove: function() {
            this.removeMap();
            Backbone.View.prototype.remove.call(this);
        },

        getShapes: function() {
            const shapes = []
            if (this.drawnItems) {
                _.each(this.drawnItems.getLayers(), function(layer){
                    let shape;

                    if (layer instanceof leaflet.Circle) {
                        const latLng = layer.getLatLng();
                        shape = { type: 'circle', center: [latLng.lat, latLng.lng], radius: layer.getRadius() };
                    }
                    else if (layer instanceof leaflet.Polygon) {
                        shape = { type: 'polygon', points: _.map(layer.getLatLngs()[0], function(latLng){
                            return [latLng.lat, latLng.lng]
                        }) };
                    }

                    if (shape) {
                        if (layer.negated) {
                            shape.NOT = true;
                        }
                        if (layer.spatial) {
                            shape.spatial = layer.spatial;
                        }
                        shapes.push(shape);
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
