/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/geography-model',
    'find/app/configuration'
], function(GeographyModel, configuration) {
    'use strict';

    function resetLocationFields() {
        GeographyModel.LocationFields.length = 0;
        _.each(GeographyModel.LocationFieldsById, function(val, key){
            delete GeographyModel.LocationFieldsById[key];
        });
    }

    describe('Geography Model', function() {
        beforeEach(function() {
            resetLocationFields();

            GeographyModel.parseConfiguration({
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
            })

            this.model = new GeographyModel({});
        });

        afterEach(function(){
            resetLocationFields();
            GeographyModel.parseConfiguration(configuration());
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

            it('returns POLYGON for a polygon', function() {
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
        });
    });
});
