define([
    'leaflet', 'leaflet.draw'
], function(L, leafletDraw){

    const DEFAULT_INTERSECTION_TYPES = [
        'within',
        'intersect',
        'contains'
    ];

    L.drawLocal.edit.toolbar.buttons.polygonSpatial = 'Polygon within/intersects/contains toggle';
    L.drawLocal.edit.toolbar.buttons.polygonSpatialDisabled = 'No layers to change the polygon type of.';
    L.drawLocal.edit.handlers.polygonSpatial = {
        tooltip: {
            text: 'Click/ctrl-click on a polygon to change between within/intersects/contains'
        }
    }

    L.Draw.Event.POLYGONSPATIALSTART = 'draw:polygonspatialstart';
    L.Draw.Event.POLYGONSPATIALSTOP = 'draw:polygonspatialstop';

    function isPolygonLayer(layer) {
        return layer instanceof L.Polygon;
    }

    /**
     * @class L.EditToolbar.PolygonSpatial
     * @aka EditToolbar.PolygonSpatial
     */
    L.EditToolbar.PolygonSpatial = L.Handler.extend({
        statics: {
            TYPE: 'polygonSpatial'
        },

        includes: L.Mixin.Events,

        // @method intialize(): void
        initialize: function (map, options) {
            L.Handler.prototype.initialize.call(this, map);

            L.Util.setOptions(this, options);

            // Store the selectable layer group for ease of access
            this._editableLayers = this.options.featureGroup;

            if (!(this._editableLayers instanceof L.FeatureGroup)) {
                throw new Error('options.featureGroup must be a L.FeatureGroup');
            }

            // Save the type so super can fire, need to do this as cannot do this.TYPE :(
            this.type = L.EditToolbar.PolygonSpatial.TYPE;
        },

        // @method enable(): void
        // Enable the polygonSpatial toolbar
        enable: function () {
            if (this._enabled || !this._hasAvailableLayers()) {
                return;
            }
            this.fire('enabled', { handler: this.type });

            this._map.fire(L.Draw.Event.POLYGONSPATIALSTART, { handler: this.type });

            L.Handler.prototype.enable.call(this);

            this._editableLayers
                .on('layeradd', this._enableLayerEdit, this)
                .on('layerremove', this._disableLayerEdit, this);
        },

        // @method disable(): void
        // Disable the negation toolbar
        disable: function () {
            if (!this._enabled) {
                return;
            }

            this._editableLayers
                .off('layeradd', this._enableLayerEdit, this)
                .off('layerremove', this._disableLayerEdit, this);

            L.Handler.prototype.disable.call(this);

            this._map.fire(L.Draw.Event.POLYGONSPATIALSTOP, { handler: this.type });

            this.fire('disabled', { handler: this.type });
        },

        // @method addHooks(): void
        // Add listener hooks to this handler
        addHooks: function () {
            var map = this._map;

            if (map) {
                map.getContainer().focus();

                this._editableLayers.eachLayer(this._enableLayerEdit, this);

                this._tooltip = new L.Draw.Tooltip(this._map);
                this._tooltip.updateContent({ text: L.drawLocal.edit.handlers.polygonSpatial.tooltip.text });

                this._map.on('mousemove', this._onMouseMove, this);
            }
        },

        // @method removeHooks(): void
        // Remove listener hooks from this handler
        removeHooks: function () {
            if (this._map) {
                this._editableLayers.eachLayer(this._disableLayerEdit, this);
                
                this._tooltip.dispose();
                this._tooltip = null;

                this._map.off('mousemove', this._onMouseMove, this);
            }
        },

        // @method revertLayers(): void
        // Revert the edited layers back to their prior state.
        revertLayers: function () {
        },

        // @method save(): void
        // Save edited layers
        save: function () {
        },

        _enableLayerEdit: function (e) {
            var layer = e.layer || e.target || e;

            if (isPolygonLayer(layer)) {
                layer.on('click', this._editLayer, this);
            }
        },

        _disableLayerEdit: function (e) {
            var layer = e.layer || e.target || e;

            if (isPolygonLayer(layer)) {
                layer.off('click', this._editLayer, this);
            }
        },

        _editLayer: function (e) {
            L.DomEvent.stopPropagation(e);
            L.DomEvent.preventDefault(e);
            var layer = e.layer || e.target || e;

            const intersectionTypes = this.options.shapeOptions.intersectionTypes;

            if (!layer.spatial) {
                layer.spatial = intersectionTypes[0];
            }

            const step = e.originalEvent.ctrlKey ? (intersectionTypes.length - 1) : 1;

            layer.spatial = intersectionTypes[(intersectionTypes.indexOf(layer.spatial) + step) % intersectionTypes.length];

            const colorOpts = this.options.shapeOptions.colorFn(layer);
            layer.setStyle(colorOpts)

            layer.fire('polygonSpatialChange', layer);
        },

        _onMouseMove: function (e) {
            this._tooltip.updatePosition(e.latlng);
        },

        _hasAvailableLayers: function () {
            return this._editableLayers.getLayers().filter(isPolygonLayer).length !== 0;
        }
    });
    
    L.EditToolbar.prototype.options.polygonSpatial = {
        shapeOptions: {
            intersectionTypes: DEFAULT_INTERSECTION_TYPES,
            colorFn: function(shape){
                const color = '#3388ff';
                return { color: color, fillColor: color };
            }
        }
    };

    const origGetModeHandlers = L.EditToolbar.prototype.getModeHandlers;
    L.EditToolbar.prototype.getModeHandlers = function(map){
        const handlers = origGetModeHandlers.apply(this, arguments);

        const featureGroup = this.options.featureGroup;

        handlers.unshift({
            enabled: this.options.polygonSpatial,
            handler: new L.EditToolbar.PolygonSpatial(map, {
                featureGroup: featureGroup,
                shapeOptions: this.options.polygonSpatial.shapeOptions
            }),
            title: L.drawLocal.edit.toolbar.buttons.polygonSpatial
        });

        return handlers;
    }


    const orig_checkDisabled = L.EditToolbar.prototype._checkDisabled;
    L.EditToolbar.prototype._checkDisabled = function(){
        const featureGroup = this.options.featureGroup,
            hasLayers = featureGroup.getLayers().filter(isPolygonLayer).length !== 0;

        if (this.options.polygonSpatial) {
            const button = this._modes[L.EditToolbar.PolygonSpatial.TYPE].button;

            if (hasLayers) {
                L.DomUtil.removeClass(button, 'leaflet-disabled');
            } else {
                L.DomUtil.addClass(button, 'leaflet-disabled');
            }

            button.setAttribute(
                'title',
                hasLayers ?
                    L.drawLocal.edit.toolbar.buttons.polygonSpatial
                    : L.drawLocal.edit.toolbar.buttons.polygonSpatialDisabled
            );
        }

        return orig_checkDisabled.apply(this, arguments);
    }

    const origGetActions = L.EditToolbar.prototype.getActions;
    L.EditToolbar.prototype.getActions = function(handler){
        if (handler instanceof L.EditToolbar.PolygonSpatial) {
            return [];
        }

        return origGetActions.apply(this);
    }

    return leafletDraw;
})