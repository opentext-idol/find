/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/geography-model',
    'find/app/configuration',
    'fieldtext/js/field-text-parser'
], function(GeographyModel, configuration, parser) {
    'use strict';

    const configWithTwoFields = {
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
            }
        }
    };

    describe('Geography Model', function() {
        beforeEach(function() {
            GeographyModel.parseConfiguration(configWithTwoFields)

            this.model = new GeographyModel({});
        });

        afterEach(function(){
            GeographyModel.parseConfiguration(configuration());
        })

        describe('configuration parsing', function(){
            const locationFields = GeographyModel.LocationFields;
            const locationFieldsById = GeographyModel.LocationFieldsById;

            it('should have parsed two location fields', function(){
                expect(locationFields.length).toEqual(2);
                expect(locationFieldsById['DefaultLocation']).toExist();
                expect(locationFieldsById['OGLocation']).toExist();
            })

            it('should not parse any fields for an empty configuration', function(){
                GeographyModel.parseConfiguration(null);
                expect(locationFields.length).toEqual(0);
                expect(_.keys(locationFieldsById).length).toEqual(0);
            })

            it('should not parse any fields if the map is disabled', function(){
                const config = _.clone(configWithTwoFields);
                config.map = _.clone(config.map);
                config.map.enabled = false;

                GeographyModel.parseConfiguration(config);
                expect(locationFields.length).toEqual(0);
                expect(_.keys(locationFieldsById).length).toEqual(0);
            })

            it('should not parse any fields if the map is not configured', function(){
                const config = _.clone(configWithTwoFields);
                delete config.map;

                GeographyModel.parseConfiguration(config);
                expect(locationFields.length).toEqual(0);
                expect(_.keys(locationFieldsById).length).toEqual(0);
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

            it('returns three POLYGON fieldtext (with ±360° offsets) for a polygon', function() {
                this.model.set('OGLocation', [
                    {"type":"polygon","points":[[-12.76,-206.71],[-5.09,-170.51],[-27.21,-168.75],[-29.07,-200.12]]}
                ])

                const fieldtext = this.model.toFieldText();
                expect(fieldtext.toString()).toEqual(
                    'POLYGON{-12.76,-206.71,-5.09,-170.51,-27.21,-168.75,-29.07,-200.12}:OG_LATITUDE:OG_LONGITUDE OR ' +
                    'POLYGON{-12.76,153.29,-5.09,189.49,-27.21,191.25,-29.07,159.88}:OG_LATITUDE:OG_LONGITUDE OR ' +
                    'POLYGON{-12.76,-566.71,-5.09,-530.51,-27.21,-528.75,-29.07,-560.12}:OG_LATITUDE:OG_LONGITUDE');
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
                    'POLYGON{-12.76,-206.71,-5.09,-170.51,-27.21,-168.75,-29.07,-200.12}:OG_LATITUDE:OG_LONGITUDE OR ' +
                    'POLYGON{-12.76,153.29,-5.09,189.49,-27.21,191.25,-29.07,159.88}:OG_LATITUDE:OG_LONGITUDE OR ' +
                    'POLYGON{-12.76,-566.71,-5.09,-530.51,-27.21,-528.75,-29.07,-560.12}:OG_LATITUDE:OG_LONGITUDE' +
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
                    'POLYGON{-12.76,-206.71,-5.09,-170.51,-27.21,-168.75,-29.07,-200.12}:OG_LATITUDE:OG_LONGITUDE OR ' +
                    'POLYGON{-12.76,153.29,-5.09,189.49,-27.21,191.25,-29.07,159.88}:OG_LATITUDE:OG_LONGITUDE OR ' +
                    'POLYGON{-12.76,-566.71,-5.09,-530.51,-27.21,-528.75,-29.07,-560.12}:OG_LATITUDE:OG_LONGITUDE'
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
                    'POLYGON{-12.76,-206.71,-5.09,-170.51,-27.21,-168.75,-29.07,-200.12}:OG_LATITUDE:OG_LONGITUDE OR ' +
                    'POLYGON{-12.76,153.29,-5.09,189.49,-27.21,191.25,-29.07,159.88}:OG_LATITUDE:OG_LONGITUDE OR ' +
                    'POLYGON{-12.76,-566.71,-5.09,-530.51,-27.21,-528.75,-29.07,-560.12}:OG_LATITUDE:OG_LONGITUDE' +
                    ') AND NOT (' +
                    'DISTSPHERICAL{40.123,60.321,123}:OG_LATITUDE:OG_LONGITUDE OR ' +
                    'POLYGON{50.76,-206.71,11.12,-170.51,17,-168.75}:OG_LATITUDE:OG_LONGITUDE OR ' +
                    'POLYGON{50.76,153.29,11.12,189.49,17,191.25}:OG_LATITUDE:OG_LONGITUDE OR ' +
                    'POLYGON{50.76,-566.71,11.12,-530.51,17,-528.75}:OG_LATITUDE:OG_LONGITUDE' +
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

        describe('appendFieldText function', function(){
            const original = new parser.ExpressionNode('MATCH', ['field'], ['value']);

            it('should return null if both the original fieldtext and the model are empty', function(){
                expect(this.model.appendFieldText(null)).toBeNull();
            })

            it('should return original fieldtext if the model is empty', function(){
                expect(this.model.appendFieldText(original).toString()).toEqual('MATCH{value}:field');
            })

            it('should return fieldtext if original fieldtext is empty', function(){
                this.model.set('OGLocation', [
                    {"type":"circle","center":[-7.013,-193.007],"radius":3511716.726}]
                )

                expect(this.model.appendFieldText(null).toString()).toEqual(
                    'DISTSPHERICAL{-7.013,-193.007,3512}:OG_LATITUDE:OG_LONGITUDE');
            })

            it('should return AND of geographic restriction filters and original fieldtext', function(){
                this.model.set('OGLocation', [
                    {"type":"circle","center":[-7.013,-193.007],"radius":3511716.726}]
                )

                expect(this.model.appendFieldText(original).toString()).toEqual(
                    'MATCH{value}:field AND DISTSPHERICAL{-7.013,-193.007,3512}:OG_LATITUDE:OG_LONGITUDE');
            })

            it('should correctly parenthesize the OR when multiple shape fieldtext are added to original fields', function(){
                this.model.set('OGLocation', [
                    {"type":"circle","center":[-7.013,-193.007],"radius":3511716.726},
                    {"type":"circle","center":[40.123,60.321],"radius":123456.1}
                ])

                expect(this.model.appendFieldText(original).toString()).toEqual(
                    'MATCH{value}:field AND ' +
                    '(DISTSPHERICAL{-7.013,-193.007,3512}:OG_LATITUDE:OG_LONGITUDE OR ' +
                    'DISTSPHERICAL{40.123,60.321,123}:OG_LATITUDE:OG_LONGITUDE)');
            })
        })
    });
});
