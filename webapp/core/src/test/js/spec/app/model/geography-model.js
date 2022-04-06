/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/geography-model',
    'find/app/configuration',
    'fieldtext/js/field-text-parser'
], function(GeographyModel, configuration, parser) {
    'use strict';

    const configWithThreeFields = {
        map: {
            "enabled" : true,
            "locationFields" : [
                {
                    "id": "DefaultLocation",
                    "displayName": "Default Location",
                    "latitudeField": "latitude",
                    "longitudeField": "longitude",
                    "iconName": null,
                    "iconColor": null,
                    "markerColor": null
                },
                {
                    "id": "OGLocation",
                    "displayName": "OG Location",
                    "latitudeField": "oglatitude",
                    "longitudeField": "oglongitude",
                    "iconName": "hp-pin",
                    "iconColor": "blue",
                    "markerColor": "orange"
                },
                {
                    "id": "GeoindexLocation",
                    "displayName": "Geoindex Location",
                    "geoindexField": "geounified",
                    "iconName" : "hp-navigate",
                    "iconColor" : "green",
                    "markerColor" : "red"
                }
            ]
        },
        fieldsInfo: {
            "latitude": {
                "names": [
                    "NODE_PLACE/LAT",
                    "LAT"
                ],
                "type": "NUMBER",
                "advanced": true
            },
            "longitude": {
                "names": [
                    "NODE_PLACE/LON",
                    "LON"
                ],
                "type": "NUMBER",
                "advanced": true
            },
            "oglatitude": {
                "names": [
                    "OG_LATITUDE"
                ],
                "type": "NUMBER",
                "advanced": true
            },
            "oglongitude": {
                "names": [
                    "OG_LONGITUDE"
                ],
                "type": "NUMBER",
                "advanced": true
            },
            "geounified": {
                "names": [
                    "GEOUNIFIED"
                ],
                "type": "GEOINDEX",
                "advanced": true
            }
        }
    };

    describe('Geography Model', function() {
        beforeEach(function() {
            GeographyModel.parseConfiguration(configWithThreeFields)

            this.model = new GeographyModel({});
        });

        afterEach(function(){
            GeographyModel.parseConfiguration(configuration());
        })

        describe('configuration parsing', function(){
            const locationFields = GeographyModel.LocationFields;
            const locationFieldsById = GeographyModel.LocationFieldsById;

            it('should have parsed three location fields', function(){
                expect(locationFields.length).toEqual(3);
                expect(locationFieldsById['DefaultLocation']).toExist();
                expect(locationFieldsById['OGLocation']).toExist();
                expect(locationFieldsById['GeoindexLocation']).toExist();
            })

            it('should not parse any fields for an empty configuration', function(){
                GeographyModel.parseConfiguration(null);
                expect(locationFields.length).toEqual(0);
                expect(_.keys(locationFieldsById).length).toEqual(0);
            })

            it('should not parse any fields if the map is disabled', function(){
                const config = _.clone(configWithThreeFields);
                config.map = _.clone(config.map);
                config.map.enabled = false;

                GeographyModel.parseConfiguration(config);
                expect(locationFields.length).toEqual(0);
                expect(_.keys(locationFieldsById).length).toEqual(0);
            })

            it('should not parse any fields if the map is not configured', function(){
                const config = _.clone(configWithThreeFields);
                delete config.map;

                GeographyModel.parseConfiguration(config);
                expect(locationFields.length).toEqual(0);
                expect(_.keys(locationFieldsById).length).toEqual(0);
            })

            it('should ignore any fields where the IDOL field config is missing', function(){
                const config = _.clone(configWithThreeFields);
                config.fieldsInfo = _.clone(config.fieldsInfo);
                delete config.fieldsInfo['latitude'];

                GeographyModel.parseConfiguration(config);
                expect(locationFields.length).toEqual(2);
                expect(locationFieldsById['DefaultLocation']).toBeUndefined();
                expect(locationFieldsById['OGLocation']).toExist();
                expect(locationFieldsById['GeoindexLocation']).toExist();
            })

            it('should work with a single field', function(){
                const config = _.clone(configWithThreeFields);
                config.map = _.clone(config.map);
                config.map.locationFields = _.clone(config.map.locationFields);
                config.map.locationFields.length = 1;

                GeographyModel.parseConfiguration(config);
                expect(locationFields.length).toEqual(1);
                expect(locationFieldsById['DefaultLocation']).toExist();
                expect(locationFieldsById['OGLocation']).toBeUndefined();
                expect(locationFieldsById['GeoindexLocation']).toBeUndefined();
            })
        })

        describe('toFieldText function', function() {
            it('returns null when no shapes are applied', function() {
                const fieldtext = this.model.toFieldText();
                expect(fieldtext).toBeNull();
            });

            it('returns null when shapes list is empty', function() {
                this.model.set('OGLocation', [])

                const fieldtext = this.model.toFieldText();
                expect(fieldtext).toBeNull();
            });

            it('returns DISTSPHERICAL for a circle', function() {
                this.model.set('OGLocation', [
                    {"type":"circle","center":[-7.013,-193.007],"radius":3511716.726}]
                )

                const fieldtext = this.model.toFieldText();
                expect(fieldtext.toString()).toEqual('DISTSPHERICAL{-7.013,-193.007,3512}:OG_LATITUDE:OG_LONGITUDE');
            });

            it('returns DISTSPHERICAL for a circle for unified fields', function() {
                this.model.set('GeoindexLocation', [
                    {"type":"circle","center":[-7.013,-193.007],"radius":3511716.726}]
                )

                const fieldtext = this.model.toFieldText();
                expect(fieldtext.toString()).toEqual('DISTSPHERICAL{-7.013,-193.007,3512}:GEOUNIFIED');
            });

            it('returns three POLYGON fieldtext (with ±360° offsets) for a polygon', function() {
                this.model.set('OGLocation', [
                    {"type":"polygon","points":[[-12.76,-206.71],[-5.09,-170.51],[-27.21,-168.75],[-29.07,-200.12]]}
                ])

                const fieldtext = this.model.toFieldText();
                expect(fieldtext.toString()).toEqual(
                    'POLYGON{-206.71,-12.76,-170.51,-5.09,-168.75,-27.21,-200.12,-29.07}:OG_LONGITUDE:OG_LATITUDE OR ' +
                    'POLYGON{153.29,-12.76,189.49,-5.09,191.25,-27.21,159.88,-29.07}:OG_LONGITUDE:OG_LATITUDE OR ' +
                    'POLYGON{-566.71,-12.76,-530.51,-5.09,-528.75,-27.21,-560.12,-29.07}:OG_LONGITUDE:OG_LATITUDE');
            });

            it('returns three POLYGON fieldtext (with ±360° offsets) for a polygon with unified fields', function() {
                this.model.set('GeoindexLocation', [
                    {"type":"polygon","points":[[-12.76,-206.71],[-5.09,-170.51],[-27.21,-168.75],[-29.07,-200.12]]}
                ])

                const fieldtext = this.model.toFieldText();
                expect(fieldtext.toString()).toEqual(
                    'POLYGON{-206.71,-12.76,-170.51,-5.09,-168.75,-27.21,-200.12,-29.07}:GEOUNIFIED OR ' +
                    'POLYGON{153.29,-12.76,189.49,-5.09,191.25,-27.21,159.88,-29.07}:GEOUNIFIED OR ' +
                    'POLYGON{-566.71,-12.76,-530.51,-5.09,-528.75,-27.21,-560.12,-29.07}:GEOUNIFIED');
            });

            it('returns three POLYGON fieldtext (with ±360° offsets) for a spatial=within polygon with unified fields', function() {
                this.model.set('GeoindexLocation', [
                    {"type":"polygon","points":[[-12.76,-206.71],[-5.09,-170.51],[-27.21,-168.75],[-29.07,-200.12]],"spatial":"within"}
                ])

                const fieldtext = this.model.toFieldText();
                expect(fieldtext.toString()).toEqual(
                    'POLYGON{-206.71,-12.76,-170.51,-5.09,-168.75,-27.21,-200.12,-29.07}:GEOUNIFIED OR ' +
                    'POLYGON{153.29,-12.76,189.49,-5.09,191.25,-27.21,159.88,-29.07}:GEOUNIFIED OR ' +
                    'POLYGON{-566.71,-12.76,-530.51,-5.09,-528.75,-27.21,-560.12,-29.07}:GEOUNIFIED');
            });

            it('returns three POLYGON fieldtext (with ±360° offsets) for a spatial=intersect polygon with unified fields', function() {
                this.model.set('GeoindexLocation', [
                    {"type":"polygon","points":[[-12.76,-206.71],[-5.09,-170.51],[-27.21,-168.75],[-29.07,-200.12]],"spatial":"intersect"}
                ])

                const fieldtext = this.model.toFieldText();
                expect(fieldtext.toString()).toEqual(
                    'GEOINTERSECTS{POLYGON%20((-206.71%20-12.76%2C%20-170.51%20-5.09%2C%20-168.75%20-27.21%2C%20-200.12%20-29.07))}:GEOUNIFIED OR ' +
                    'GEOINTERSECTS{POLYGON%20((153.29%20-12.76%2C%20189.49%20-5.09%2C%20191.25%20-27.21%2C%20159.88%20-29.07))}:GEOUNIFIED OR ' +
                    'GEOINTERSECTS{POLYGON%20((-566.71%20-12.76%2C%20-530.51%20-5.09%2C%20-528.75%20-27.21%2C%20-560.12%20-29.07))}:GEOUNIFIED');
            });

            it('returns three POLYGON fieldtext (with ±360° offsets) for a spatial=contains polygon with unified fields', function() {
                this.model.set('GeoindexLocation', [
                    {"type":"polygon","points":[[-12.76,-206.71],[-5.09,-170.51],[-27.21,-168.75],[-29.07,-200.12]],"spatial":"contains"}
                ])

                const fieldtext = this.model.toFieldText();
                expect(fieldtext.toString()).toEqual(
                    'GEOCONTAINS{POLYGON%20((-206.71%20-12.76%2C%20-170.51%20-5.09%2C%20-168.75%20-27.21%2C%20-200.12%20-29.07))}:GEOUNIFIED OR ' +
                    'GEOCONTAINS{POLYGON%20((153.29%20-12.76%2C%20189.49%20-5.09%2C%20191.25%20-27.21%2C%20159.88%20-29.07))}:GEOUNIFIED OR ' +
                    'GEOCONTAINS{POLYGON%20((-566.71%20-12.76%2C%20-530.51%20-5.09%2C%20-528.75%20-27.21%2C%20-560.12%20-29.07))}:GEOUNIFIED');
            });

            it('returns NOT DISTSPHERICAL for a circle exclusion', function() {
                this.model.set('OGLocation', [
                    {"type":"circle","center":[-7.013,-193.007],"radius":3511716.726,"NOT":true}]
                )

                const fieldtext = this.model.toFieldText();
                expect(fieldtext.toString()).toEqual('NOT DISTSPHERICAL{-7.013,-193.007,3512}:OG_LATITUDE:OG_LONGITUDE');
            });

            it('returns NOT POLYGON for a polygon exclusion', function() {
                this.model.set('OGLocation', [
                    {"type":"polygon","points":[[-12.76,-206.71],[-5.09,-170.51],[-27.21,-168.75],[-29.07,-200.12]],"NOT":true}
                ])

                const fieldtext = this.model.toFieldText();
                expect(fieldtext.toString()).toEqual(
                    'NOT (' +
                    'POLYGON{-206.71,-12.76,-170.51,-5.09,-168.75,-27.21,-200.12,-29.07}:OG_LONGITUDE:OG_LATITUDE OR ' +
                    'POLYGON{153.29,-12.76,189.49,-5.09,191.25,-27.21,159.88,-29.07}:OG_LONGITUDE:OG_LATITUDE OR ' +
                    'POLYGON{-566.71,-12.76,-530.51,-5.09,-528.75,-27.21,-560.12,-29.07}:OG_LONGITUDE:OG_LATITUDE' +
                    ')'
                );
            });

            it('returns NOT-OR of three POLYGON fieldtext (with ±360° offsets) for a polygon with unified fields', function() {
                this.model.set('GeoindexLocation', [
                    {"type":"polygon","points":[[-12.76,-206.71],[-5.09,-170.51],[-27.21,-168.75],[-29.07,-200.12]],"NOT":true}
                ])

                const fieldtext = this.model.toFieldText();
                expect(fieldtext.toString()).toEqual(
                    'NOT (' +
                    'POLYGON{-206.71,-12.76,-170.51,-5.09,-168.75,-27.21,-200.12,-29.07}:GEOUNIFIED OR ' +
                    'POLYGON{153.29,-12.76,189.49,-5.09,191.25,-27.21,159.88,-29.07}:GEOUNIFIED OR ' +
                    'POLYGON{-566.71,-12.76,-530.51,-5.09,-528.75,-27.21,-560.12,-29.07}:GEOUNIFIED' +
                    ')'
                );
            });

            it('returns NOT-OR of three POLYGON fieldtext (with ±360° offsets) for a spatial=within polygon with unified fields', function() {
                this.model.set('GeoindexLocation', [
                    {"type":"polygon","points":[[-12.76,-206.71],[-5.09,-170.51],[-27.21,-168.75],[-29.07,-200.12]],"spatial":"within","NOT":true}
                ])

                const fieldtext = this.model.toFieldText();
                expect(fieldtext.toString()).toEqual(
                    'NOT (' +
                    'POLYGON{-206.71,-12.76,-170.51,-5.09,-168.75,-27.21,-200.12,-29.07}:GEOUNIFIED OR ' +
                    'POLYGON{153.29,-12.76,189.49,-5.09,191.25,-27.21,159.88,-29.07}:GEOUNIFIED OR ' +
                    'POLYGON{-566.71,-12.76,-530.51,-5.09,-528.75,-27.21,-560.12,-29.07}:GEOUNIFIED' +
                    ')'
                );
            });

            it('returns NOT-OR of three POLYGON fieldtext (with ±360° offsets) for a spatial=intersect polygon with unified fields', function() {
                this.model.set('GeoindexLocation', [
                    {"type":"polygon","points":[[-12.76,-206.71],[-5.09,-170.51],[-27.21,-168.75],[-29.07,-200.12]],"spatial":"intersect","NOT":true}
                ])

                const fieldtext = this.model.toFieldText();
                expect(fieldtext.toString()).toEqual(
                    'NOT (' +
                    'GEOINTERSECTS{POLYGON%20((-206.71%20-12.76%2C%20-170.51%20-5.09%2C%20-168.75%20-27.21%2C%20-200.12%20-29.07))}:GEOUNIFIED OR ' +
                    'GEOINTERSECTS{POLYGON%20((153.29%20-12.76%2C%20189.49%20-5.09%2C%20191.25%20-27.21%2C%20159.88%20-29.07))}:GEOUNIFIED OR ' +
                    'GEOINTERSECTS{POLYGON%20((-566.71%20-12.76%2C%20-530.51%20-5.09%2C%20-528.75%20-27.21%2C%20-560.12%20-29.07))}:GEOUNIFIED' +
                    ')'
                );
            });

            it('returns NOT-OR of three POLYGON fieldtext (with ±360° offsets) for a spatial=contains polygon with unified fields', function() {
                this.model.set('GeoindexLocation', [
                    {"type":"polygon","points":[[-12.76,-206.71],[-5.09,-170.51],[-27.21,-168.75],[-29.07,-200.12]],"spatial":"contains","NOT":true}
                ])

                const fieldtext = this.model.toFieldText();
                expect(fieldtext.toString()).toEqual(
                    'NOT (' +
                    'GEOCONTAINS{POLYGON%20((-206.71%20-12.76%2C%20-170.51%20-5.09%2C%20-168.75%20-27.21%2C%20-200.12%20-29.07))}:GEOUNIFIED OR ' +
                    'GEOCONTAINS{POLYGON%20((153.29%20-12.76%2C%20189.49%20-5.09%2C%20191.25%20-27.21%2C%20159.88%20-29.07))}:GEOUNIFIED OR ' +
                    'GEOCONTAINS{POLYGON%20((-566.71%20-12.76%2C%20-530.51%20-5.09%2C%20-528.75%20-27.21%2C%20-560.12%20-29.07))}:GEOUNIFIED' +
                    ')'
                );
            });

            it('returns DISTSPHERICAL OR POLYGON for a circle + polygon', function() {
                this.model.set('OGLocation', [
                    {"type":"circle","center":[-7.013,-193.007],"radius":3511716.726},
                    {"type":"polygon","points":[[-12.76,-206.71],[-5.09,-170.51],[-27.21,-168.75],[-29.07,-200.12]]}
                ])

                const fieldtext = this.model.toFieldText();
                expect(fieldtext.toString()).toEqual(
                    'DISTSPHERICAL{-7.013,-193.007,3512}:OG_LATITUDE:OG_LONGITUDE OR ' +
                    'POLYGON{-206.71,-12.76,-170.51,-5.09,-168.75,-27.21,-200.12,-29.07}:OG_LONGITUDE:OG_LATITUDE OR ' +
                    'POLYGON{153.29,-12.76,189.49,-5.09,191.25,-27.21,159.88,-29.07}:OG_LONGITUDE:OG_LATITUDE OR ' +
                    'POLYGON{-566.71,-12.76,-530.51,-5.09,-528.75,-27.21,-560.12,-29.07}:OG_LONGITUDE:OG_LATITUDE'
                );
            });

            it('allow mixing multiple inclusion and exclusion filters', function() {
                this.model.set('OGLocation', [
                    {"type":"circle","center":[-7.013,-193.007],"radius":3511716.726},
                    {"type":"polygon","points":[[-12.76,-206.71],[-5.09,-170.51],[-27.21,-168.75],[-29.07,-200.12]]},
                    {"type":"circle","center":[40.123,60.321],"radius":123456.1,"NOT":true},
                    {"type":"polygon","points":[[50.76,-206.71],[11.12,-170.51],[17,-168.75]],"NOT":true}
                ])

                const fieldtext = this.model.toFieldText();
                expect(fieldtext.toString()).toEqual(
                    '(' +
                    'DISTSPHERICAL{-7.013,-193.007,3512}:OG_LATITUDE:OG_LONGITUDE OR ' +
                    'POLYGON{-206.71,-12.76,-170.51,-5.09,-168.75,-27.21,-200.12,-29.07}:OG_LONGITUDE:OG_LATITUDE OR ' +
                    'POLYGON{153.29,-12.76,189.49,-5.09,191.25,-27.21,159.88,-29.07}:OG_LONGITUDE:OG_LATITUDE OR ' +
                    'POLYGON{-566.71,-12.76,-530.51,-5.09,-528.75,-27.21,-560.12,-29.07}:OG_LONGITUDE:OG_LATITUDE' +
                    ') AND NOT (' +
                    'DISTSPHERICAL{40.123,60.321,123}:OG_LATITUDE:OG_LONGITUDE OR ' +
                    'POLYGON{-206.71,50.76,-170.51,11.12,-168.75,17}:OG_LONGITUDE:OG_LATITUDE OR ' +
                    'POLYGON{153.29,50.76,189.49,11.12,191.25,17}:OG_LONGITUDE:OG_LATITUDE OR ' +
                    'POLYGON{-566.71,50.76,-530.51,11.12,-528.75,17}:OG_LONGITUDE:OG_LATITUDE' +
                    ')'
                );
            });

            it('returns one DISTSPHERICAL per IDOL field for documents with multiple fields', function() {
                this.model.set('DefaultLocation', [
                    {"type":"circle","center":[-7.013,-193.007],"radius":3511716.726}]
                )

                const fieldtext = this.model.toFieldText();
                expect(fieldtext.toString()).toEqual(
                    'DISTSPHERICAL{-7.013,-193.007,3512}:NODE_PLACE/LAT:NODE_PLACE/LON OR ' +
                    'DISTSPHERICAL{-7.013,-193.007,3512}:LAT:LON'
                );
            });

            it('returns AND-ed geographic restrictions if multiple fields have geographic restrictions', function() {
                this.model.set('DefaultLocation', [
                    {"type":"circle","center":[-7.013,-193.007],"radius":3511716.726}]
                )
                this.model.set('OGLocation', [
                    {"type":"circle","center":[40.123,60.321],"radius":123456.1}]
                )

                const fieldtext = this.model.toFieldText();
                expect(fieldtext.toString()).toEqual(
                    '(' +
                    'DISTSPHERICAL{-7.013,-193.007,3512}:NODE_PLACE/LAT:NODE_PLACE/LON OR ' +
                    'DISTSPHERICAL{-7.013,-193.007,3512}:LAT:LON' +
                    ') AND ' +
                    'DISTSPHERICAL{40.123,60.321,123}:OG_LATITUDE:OG_LONGITUDE'
                );
            });

            it('ignores geographic restrictions from fields which are not configured (e.g. deleted fields in a saved search)', function() {
                this.model.set('NoSuchField', [
                    {"type":"circle","center":[-7.013,-193.007],"radius":3511716.726}]
                )

                expect(this.model.toFieldText()).toBeNull();

                this.model.set('OGLocation', [
                    {"type":"circle","center":[40.123,60.321],"radius":123456.1}]
                )

                const fieldtext = this.model.toFieldText();
                expect(fieldtext.toString()).toEqual(
                    'DISTSPHERICAL{40.123,60.321,123}:OG_LATITUDE:OG_LONGITUDE'
                );
            });
        });
    });
});
