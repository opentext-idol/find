define([
    'find/app/model/document-model',
    'moment'
], function(DocumentModel, moment) {

    var THUMBNAIL = 'VGhlIGJhc2UgNjQgZW5jb2RlZCB0aHVtYm5haWw=';
    var TITLE = 'My Document';
    var TRANSCRIPT = 'test transcript';
    var SOURCETYPE = 'news';
    var DATE_PUBLISHED_SECONDS = 1456161196000;

    function baseResponse() {
        return {
            fieldMap: {
                authors: {type: 'STRING', displayName: 'Author', values: ['Humbert', 'Gereon']},
                longitude: {type: 'NUMBER', displayName: 'Longitude', values: ['52.5']},
                thumbnail: {type: 'STRING', values: [THUMBNAIL]},
                datePublished: {type: 'DATE', displayName: 'Date Published', values: [DATE_PUBLISHED_SECONDS]},
                sourceType: {type: 'STRING', values: [SOURCETYPE]},
                transcript: {type: 'STRING', values: [TRANSCRIPT]}
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
                var reference = 'http://example.com/main/';

                expect(this.parse(_.extend({
                    reference: reference
                }, baseResponse())).title).toBe(reference);
            });

            it('uses the whole reference if the reference finishes with a slash followed by whitespace', function() {
                var reference = 'foo/   \n ';

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
                var fields = this.parse(fullResponse()).fields;
                expect(fields.length).toBe(6);

                var longitudeField = _.findWhere(fields, {displayName: 'Longitude'});
                expect(longitudeField).toBeDefined();
                expect(longitudeField.values.length).toBe(1);
                expect(longitudeField.values[0]).toBe(52.5);

                var datePublishedField = _.findWhere(fields, {displayName: 'Date Published'});
                expect(datePublishedField).toBeDefined();
                expect(datePublishedField.values.length).toBe(1);
                expect(datePublishedField.values[0]).toBe(moment(DATE_PUBLISHED_SECONDS).format('LLLL'));
            });
        });
    });

});
