/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/document-model',
    'find/app/configuration',
    'moment',
    'underscore'
], function(DocumentModel, configuration, moment, _) {
    'use strict';

    const THUMBNAIL = 'VGhlIGJhc2UgNjQgZW5jb2RlZCB0aHVtYm5haWw=';
    const TITLE = 'My Document';
    const TRANSCRIPT = 'test transcript';
    const SOURCETYPE = 'news';
    const DATE_PUBLISHED_SECONDS = 1456161196000;

    function baseResponse() {
        return {
            fieldMap: {
                authors: {type: 'STRING', displayName: 'Author', values: [{value: 'Humbert', displayValue: 'Humbert'}, {value: 'Gereon', displayValue: 'Gereon'}]},
                LONGITUDE: {type: 'NUMBER', displayName: 'Longitude', values: [{value: 52.5, displayValue: '52.5'}]},
                LATITUDE: {type: 'NUMBER', displayName: 'Latitude', values: [{value: 42.2, displayValue: '42.2'}]},
                thumbnail: {type: 'STRING', displayName: 'thumbnail', values: [{value: THUMBNAIL, displayValue: THUMBNAIL}]},
                datePublished: {type: 'DATE', displayName: 'Date Published', values: [{value: DATE_PUBLISHED_SECONDS}]},
                sourceType: {type: 'STRING', displayName: 'sourceType', values: [{value: SOURCETYPE, displayValue: SOURCETYPE}]},
                transcript: {type: 'STRING', displayName: 'transcript', values: [{value: TRANSCRIPT, displayValue: TRANSCRIPT}]}
            }
        };
    }

    function fullResponse() {
        return _.extend({
            reference: 'my-document',
            title: TITLE
        }, baseResponse());
    }

    describe('Document model', function() {
        describe('parse method', function() {
            beforeEach(function() {
                this.parse = DocumentModel.prototype.parse;

                configuration.and.returnValue({
                    map: {
                        enabled: true,
                        locationFields: [{
                            displayName: 'test',
                            latitudeField: 'LATITUDE',
                            longitudeField: 'LONGITUDE'
                        }]
                    }
                })
            });

            afterEach(function() {
                configuration.and.stub();
            });

            it('uses the title from the response if present', function() {
                expect(this.parse(fullResponse()).title).toBe(TITLE);
            });

            it('uses the last part of a windows file path as the title if there is no title', function() {
                expect(this.parse(_.extend({
                    reference: 'C:\\Documents\\file.txt'
                }, baseResponse())).title).toBe('file.txt');
            });

            it('uses the last part of a unix file path as the title if there is no title', function() {
                expect(this.parse(_.extend({
                    reference: '/home/user/another-file.txt'
                }, baseResponse())).title).toBe('another-file.txt');
            });

            it('uses the whole reference if the reference finishes with a slash', function() {
                const reference = 'http://example.com/main/';

                expect(this.parse(_.extend({
                    reference: reference
                }, baseResponse())).title).toBe(reference);
            });

            it('uses the whole reference if the reference finishes with a slash followed by whitespace', function() {
                const reference = 'foo/   \n ';

                expect(this.parse(_.extend({
                    reference: reference
                }, baseResponse())).title).toBe(reference);
            });

            it('parses the authors from the field map', function() {
                expect(this.parse(fullResponse()).authors).toEqual(['Humbert', 'Gereon']);
            });

            it('parses the thumbnail from the field map', function() {
                expect(this.parse(fullResponse()).thumbnail).toBe(THUMBNAIL);
            });

            it('parses the sourceType from the field map', function() {
                expect(this.parse(fullResponse()).sourceType).toBe(SOURCETYPE);
            });

            it('parses the transcript from the field map', function() {
                expect(this.parse(fullResponse()).transcript).toBe(TRANSCRIPT);
            });

            it('parses the field map into an array, converting date values to formatted strings and number values to javascript numbers', function() {
                const fields = this.parse(fullResponse()).fields;
                expect(fields.length).toBe(7);

                const longitudeField = _.findWhere(fields, {displayName: 'Longitude'});
                expect(longitudeField).toBeDefined();
                expect(longitudeField.values.length).toBe(1);
                expect(longitudeField.values[0]).toBe(52.5);

                const datePublishedField = _.findWhere(fields, {displayName: 'Date Published'});
                expect(datePublishedField).toBeDefined();
                expect(datePublishedField.values.length).toBe(1);
                expect(datePublishedField.values[0]).toBe(moment(DATE_PUBLISHED_SECONDS).format('LLLL'));
            });

            it('parses the locations from the field map', function() {
                const locations = this.parse(fullResponse()).locations;
                expect(locations.length).toBe(1);

                const location = locations[0];

                expect(location).toBeDefined();
                expect(location.displayName).toBe('test');
                expect(location.latitude).toBe(42.2);
                expect(location.longitude).toBe(52.5);
            });
        });
    });
});
