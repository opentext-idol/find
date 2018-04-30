define([
    'leaflet', 'leaflet.draw'
], function(L, leafletDraw){

    L.drawLocal.edit.toolbar.buttons.negate = 'Negate layers.';
    L.drawLocal.edit.toolbar.buttons.negateDisabled = 'No layers to negate.';
    L.drawLocal.edit.handlers.negate = {
        tooltip: {
            text: 'Click on a shape to negate'
        }
    }

    L.Draw.Event.NEGATESTART = 'draw:negatestart';
    L.Draw.Event.NEGATESTOP = 'draw:negatestop';

    /**
     * @class L.EditToolbar.Negate
     * @aka EditToolbar.Negate
     */
    L.EditToolbar.Negate = L.Handler.extend({
        statics: {
            TYPE: 'negate'
        },

        includes: L.Mixin.Events,

        // @method intialize(): void
        initialize: function (map, options) {
            L.Handler.prototype.initialize.call(this, map);

            L.Util.setOptions(this, options);

            // Store the selectable layer group for ease of access
            this._negatableLayers = this.options.featureGroup;

            if (!(this._negatableLayers instanceof L.FeatureGroup)) {
                throw new Error('options.featureGroup must be a L.FeatureGroup');
            }

            // Save the type so super can fire, need to do this as cannot do this.TYPE :(
            this.type = L.EditToolbar.Negate.TYPE;
        },

        // @method enable(): void
        // Enable the negate toolbar
        enable: function () {
            if (this._enabled || !this._hasAvailableLayers()) {
                return;
            }
            this.fire('enabled', { handler: this.type });

            this._map.fire(L.Draw.Event.NEGATESTART, { handler: this.type });

            L.Handler.prototype.enable.call(this);

            this._negatableLayers
                .on('layeradd', this._enableLayerNegate, this)
                .on('layerremove', this._disableLayerNegate, this);
        },

        // @method disable(): void
        // Disable the negation toolbar
        disable: function () {
            if (!this._enabled) {
                return;
            }

            this._negatableLayers
                .off('layeradd', this._enableLayerNegate, this)
                .off('layerremove', this._disableLayerNegate, this);

            L.Handler.prototype.disable.call(this);

            this._map.fire(L.Draw.Event.NEGATESTOP, { handler: this.type });

            this.fire('disabled', { handler: this.type });
        },

        // @method addHooks(): void
        // Add listener hooks to this handler
        addHooks: function () {
            var map = this._map;

            if (map) {
                map.getContainer().focus();

                this._negatableLayers.eachLayer(this._enableLayerNegate, this);

                this._tooltip = new L.Draw.Tooltip(this._map);
                this._tooltip.updateContent({ text: L.drawLocal.edit.handlers.negate.tooltip.text });

                this._map.on('mousemove', this._onMouseMove, this);
            }
        },

        // @method removeHooks(): void
        // Remove listener hooks from this handler
        removeHooks: function () {
            if (this._map) {
                this._negatableLayers.eachLayer(this._disableLayerNegate, this);
                
                this._tooltip.dispose();
                this._tooltip = null;

                this._map.off('mousemove', this._onMouseMove, this);
            }
        },

        // @method revertLayers(): void
        // Revert the negated layers back to their prior state.
        revertLayers: function () {
        },

        // @method save(): void
        // Save negated layers
        save: function () {
        },

        _enableLayerNegate: function (e) {
            var layer = e.layer || e.target || e;

            layer.on('click', this._negateLayer, this);
        },

        _disableLayerNegate: function (e) {
            var layer = e.layer || e.target || e;

            layer.off('click', this._negateLayer, this);
        },

        _negateLayer: function (e) {
            L.DomEvent.stopPropagation(e);
            L.DomEvent.preventDefault(e);
            var layer = e.layer || e.target || e;

            layer.negated = !layer.negated;

            const colorOpts = this.options.shapeOptions.colorFn(layer);
            layer.setStyle(colorOpts)

            layer.fire('negated', layer);
        },

        _onMouseMove: function (e) {
            this._tooltip.updatePosition(e.latlng);
        },

        _hasAvailableLayers: function () {
            return this._negatableLayers.getLayers().length !== 0;
        }
    });
    
    L.EditToolbar.prototype.options.negate = {
        shapeOptions: {
            colorFn: function(shape){
                const color = shape && shape.negated ? '#ff0000' : '#3388ff';
                return { color: color, fillColor: color };
            }
        }
    };

    const origGetModeHandlers = L.EditToolbar.prototype.getModeHandlers;
    L.EditToolbar.prototype.getModeHandlers = function(map){
        const handlers = origGetModeHandlers.apply(this, arguments);

        const featureGroup = this.options.featureGroup;

        handlers.unshift({
            enabled: this.options.negate,
            handler: new L.EditToolbar.Negate(map, {
                featureGroup: featureGroup,
                shapeOptions: this.options.negate.shapeOptions
            }),
            title: L.drawLocal.edit.toolbar.buttons.negate
        });

        return handlers;
    }


    const orig_checkDisabled = L.EditToolbar.prototype._checkDisabled;
    L.EditToolbar.prototype._checkDisabled = function(){
        const featureGroup = this.options.featureGroup,
            hasLayers = featureGroup.getLayers().length !== 0;

        if (this.options.negate) {
            const button = this._modes[L.EditToolbar.Negate.TYPE].button;

            if (hasLayers) {
                L.DomUtil.removeClass(button, 'leaflet-disabled');
            } else {
                L.DomUtil.addClass(button, 'leaflet-disabled');
            }

            button.setAttribute(
                'title',
                hasLayers ?
                    L.drawLocal.edit.toolbar.buttons.negate
                    : L.drawLocal.edit.toolbar.buttons.negateDisabled
            );
        }

        return orig_checkDisabled.apply(this, arguments);
    }

    const origGetActions = L.EditToolbar.prototype.getActions;
    L.EditToolbar.prototype.getActions = function(handler){
        if (handler instanceof L.EditToolbar.Negate) {
            return [];
        }

        return origGetActions.apply(this);
    }

    return leafletDraw;
})