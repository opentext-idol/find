/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'underscore',
    'moment',
    'find/app/model/document-model',
    'find/app/configuration'
], function(_, moment, DocumentModel, configuration) {
    'use strict';

    const THUMBNAIL = 'VGhlIGJhc2UgNjQgZW5jb2RlZCB0aHVtYm5haWw=';
    const TITLE = 'My Document';
    const TRANSCRIPT = 'test transcript';
    const SOURCETYPE = 'news';
    const DATE_PUBLISHED_SECONDS = 1456161196000;

    function baseResponse() {
        return {
            fieldMap: {
                authors: {
                    type: 'STRING',
                    displayName: 'Author',
                    values: [{value: 'Humbert', displayValue: 'Humbert'}, {value: 'Gereon', displayValue: 'Gereon'}]
                },
                LONGITUDE: {type: 'NUMBER', displayName: 'Longitude', values: [{value: 52.5, displayValue: '52.5'}]},
                LATITUDE: {type: 'NUMBER', displayName: 'Latitude', values: [{value: 42.2, displayValue: '42.2'}]},
                thumbnail: {
                    type: 'STRING',
                    displayName: 'thumbnail',
                    values: [{value: THUMBNAIL, displayValue: THUMBNAIL}]
                },
                datePublished: {type: 'DATE', displayName: 'Date Published', values: [{value: DATE_PUBLISHED_SECONDS}]},
                sourceType: {
                    type: 'STRING',
                    displayName: 'sourceType',
                    values: [{value: SOURCETYPE, displayValue: SOURCETYPE}]
                },
                transcript: {
                    type: 'STRING',
                    displayName: 'transcript',
                    values: [{value: TRANSCRIPT, displayValue: TRANSCRIPT}]
                },
                aRecord: {
                    type: 'RECORD',
                    displayName: 'a record',
                    values: [{ value: { record: 'value' }, displayValue: 'the record' }]
                }
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
                configuration.reset();
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
                expect(fields).toHaveLength(8);

                const longitudeField = _.findWhere(fields, {displayName: 'Longitude'});
                expect(longitudeField).toBeDefined();
                expect(longitudeField.values).toHaveLength(1);
                expect(longitudeField.values[0]).toBe(52.5);

                const datePublishedField = _.findWhere(fields, {displayName: 'Date Published'});
                expect(datePublishedField).toBeDefined();
                expect(datePublishedField.values).toHaveLength(1);
                expect(datePublishedField.values[0]).toBe(moment(DATE_PUBLISHED_SECONDS).format('LLLL'));
            });

            it('parses a record type from the field map', function() {
                const fields = this.parse(fullResponse()).fields;
                const recordField = _.findWhere(fields, { displayName: 'a record' });
                expect(recordField).toBeDefined();
                expect(recordField.values).toHaveLength(1);
                expect(recordField.values[0]).toEqual({ record: 'value' });
            });

            it('parses the locations from the field map', function() {
                const locations = this.parse(fullResponse()).locations;
                expect(locations).toBeDefined();
                expect(locations.test).toBeDefined();
                expect(locations.test).toHaveLength(1);

                const location = locations.test[0];

                expect(location).toBeDefined();
                expect(location.displayName).toBe('test');
                expect(location.latitude).toBe(42.2);
                expect(location.longitude).toBe(52.5);
            });
        });
    });
});
