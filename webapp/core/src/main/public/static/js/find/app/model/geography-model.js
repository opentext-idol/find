define([
    'backbone',
    'underscore',
    'fieldtext/js/field-text-parser',
    'find/app/configuration'
], function(Backbone, _, parser, configuration) {

    function MapLatLonFields(id, name, latLonFieldPairs, cfg) {
        this.id = id;
        this.displayName = name;
        this.fields = latLonFieldPairs;
        this.iconName = cfg.iconName;
        this.iconColor = cfg.iconColor;
        this.markerColor = cfg.markerColor;
    }

    const locationFields = [];
    const locationFieldsById = {};

    parseConfiguration(configuration());

    function parseConfiguration(config) {
        if (config && config.map && config.map.enabled && config.map.locationFields && config.fieldsInfo) {
            const fieldsInfo = config.fieldsInfo;

            function getFieldName(fieldName) {
                const fieldMeta = fieldsInfo[fieldName];
                if (fieldMeta && fieldMeta.names && fieldMeta.names.length) {
                    return fieldMeta.names;
                }
            }

            _.each(config.map.locationFields, function(field){
                const latField = field.latitudeField;
                const lonField = field.longitudeField;

                const latFields = getFieldName(latField);
                const lonFields = getFieldName(lonField);

                if (latFields && lonFields) {
                    const latLonFields = [];

                    for (let ii = 0, max = Math.min(latFields.length, lonFields.length); ii < max; ++ii) {
                        latLonFields.push([latFields[ii], lonFields[ii]])
                    }

                    if (latLonFields.length) {
                        const name = field.displayName;
                        const id = field.id || name;
                        const newField = new MapLatLonFields(id, name, latLonFields, field);

                        // We avoid having any two fields with the same id.
                        if (!locationFieldsById.hasOwnProperty(id)) {
                            locationFields.push(newField);
                            locationFieldsById[id] = newField;
                        }
                    }
                }
            })
        }
    }

    return Backbone.Model.extend({
        /**
         * @typedef {Object} GeographyModelAttributes
         * This is a attribute-value map of location field IDs to shape lists.
         */
        /**
         * @type GeographyModelAttributes
         */
        defaults: _.mapObject(locationFieldsById, function(){ return [] }),

        appendFieldText: function(existingFieldText){
            const toAppend = this.toFieldText();

            if (toAppend) {
                return existingFieldText ? existingFieldText.AND(toAppend) : toAppend;
            }

            return existingFieldText;
        },

        /**
         * Convert this model to fieldtext queries
         */
        toFieldText: function() {
            const allLocationFields = _.compact(_.map(locationFieldsById, function(locationField) {
                const shapes = this.get(locationField.id);
                if (!shapes || !shapes.length) {
                    return null;
                }

                const latLonFields = locationField.fields;
                const fieldNodes = [];
                const negatedFieldNodes = [];

                _.each(shapes, function (shape) {
                    const toAdd = shape.NOT ? negatedFieldNodes : fieldNodes;
                    if (shape.type === 'circle') {
                        // IDOL uses kilometers, while leaflet uses meters.
                        const km = Math.round(shape.radius / 1000);

                        _.each(latLonFields, function(fieldPair) {
                            toAdd.push(new parser.ExpressionNode('DISTSPHERICAL', fieldPair, [
                                shape.center[0],
                                shape.center[1],
                                km
                            ]));
                        })
                    }
                    else if (shape.type === 'polygon') {
                        const points = _.flatten(shape.points);
                        // We need to account for wrap-around the international date line, since we're using cartesian
                        //   polygon lookup but the world can wrap around.
                        // Test with locations e.g. Fiji 17.7134° S, 178.0650° E and Samoa 14.2710° S, 170.1322° W,
                        //   a polygon search drawn over both should find both, but without this, it'll only find one.
                        // You have to test both cases (scrolling left, and scrolling right).
                        const plus360 = points.slice(0);
                        const minus360 = points.slice(0);
                        for (let ii = 1, max = points.length; ii < max; ii += 2) {
                            plus360[ii] += 360;
                            minus360[ii] -= 360;
                        }

                        _.each(latLonFields, function(fieldPair) {
                            toAdd.push(new parser.ExpressionNode('POLYGON', fieldPair, points));
                            toAdd.push(new parser.ExpressionNode('POLYGON', fieldPair, plus360));
                            toAdd.push(new parser.ExpressionNode('POLYGON', fieldPair, minus360));
                        });
                    }
                });

                function coalesceOR(nodes) {
                    return nodes.length ? _.reduce(nodes, parser.OR) : null;
                }

                const fieldText = coalesceOR(fieldNodes);
                const negateFieldText = coalesceOR(negatedFieldNodes);

                return parser.AND(fieldText, negateFieldText && negateFieldText.NOT());
            }, this));

            return allLocationFields.length ? _.reduce(allLocationFields, parser.AND) : null;
        }
    }, {
        LocationFields: locationFields,
        LocationFieldsById: locationFieldsById,
        // this is only exposed for use by unit tests
        parseConfiguration: parseConfiguration
    });

});