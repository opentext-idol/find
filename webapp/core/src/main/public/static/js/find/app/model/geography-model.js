define([
    'backbone',
    'fieldtext/js/field-text-parser',
    'find/app/configuration'
], function(Backbone, parser, configuration) {

    function MapLatLonFields(id, name, latLonFieldPairs, cfg) {
        this.id = id;
        this.name = name;
        this.fields = latLonFieldPairs;
        this.cfg = cfg;
    }

    const config = configuration();
    const fieldsInfo = config.fieldsInfo;

    const locationFields = [];
    const locationFieldsById = {};

    function getFieldName(fieldName) {
        const fieldMeta = fieldsInfo[fieldName];
        if (fieldMeta && fieldMeta.names && fieldMeta.names.length) {
            return fieldMeta.names;
        }
    }

    if (config.map && config.map.locationFields) {
        _.each(config.map.locationFields, function(field){
            const latField = field.latitudeField;
            const lonField = field.longitudeField;

            const latFields = getFieldName(latField);
            const lonFields = getFieldName(lonField);

            if (latFields && lonFields) {
                const latLonFields = [];

                for (var ii = 0, max = Math.min(latFields.length, lonFields.length); ii < max; ++ii) {
                    latLonFields.push([latFields[ii], lonFields[ii]])
                }

                if (latLonFields.length) {
                    const name = field.displayName;
                    const id = field.id || name;
                    const newField = new MapLatLonFields(id, name, latLonFields, field);
                    locationFields.push(newField);
                    locationFieldsById[id] = newField;
                }
            }
        })
    }

    return Backbone.Model.extend({
        /**
         * @typedef {Object} GeographyModelAttributes
         * @property {?Array} shapes
         */
        /**
         * @type GeographyModelAttributes
         */
        defaults: {
            shapes: []
        },

        appendFieldText: function(existingFieldText){
            const toAppend = this.toFieldText();

            if (toAppend) {
                return existingFieldText ? toAppend.AND(toAppend) : toAppend;
            }

            return existingFieldText;
        },

        /**
         * Convert this model to fieldtext queries
         */
        toFieldText: function() {
            const locationField = locationFields[0];
            const shapes = this.get('shapes');
            if (!shapes || !shapes.length || !locationField) {
                return null;
            }

            const latLonFields = locationField.fields;

            const fieldNodes = [];

            _.each(shapes, function (shape) {
                if (shape.type === 'circle') {
                    // IDOL uses kilometers, while leaflet uses meters.
                    const km = Math.round(shape.radius / 1000);

                    _.each(latLonFields, function(fieldPair) {
                        fieldNodes.push(new parser.ExpressionNode('DISTSPHERICAL', fieldPair, [
                            shape.center[0],
                            shape.center[1],
                            km
                        ]));
                    })
                }
                else if (shape.type === 'polygon') {
                    var points = _.flatten(shape.points);
                    _.each(latLonFields, function(fieldPair) {
                        fieldNodes.push(new parser.ExpressionNode('POLYGON', fieldPair, points));
                    });
                }
            });

            if (fieldNodes.length) {
                return _.reduce(fieldNodes, parser.OR);
            } else {
                return null;
            }
        }
    }, {
        LocationFields: locationFields,
        LocationFieldsById: locationFieldsById
    });

});