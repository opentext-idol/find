/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'moment'
], function(Backbone, _, moment) {

    var MEDIA_TYPES = ['audio', 'image', 'video'];
    var WEB_TYPES = ['text/html', 'text/xhtml'];

    var fieldTypeParsers = {
        STRING: _.identity,
        NUMBER: Number,
        BOOLEAN: function(value) {
            return value.toLowerCase() === 'true';
        },
        DATE: function(value) {
            return moment(value).format('LLLL');
        }
    };

    function getMediaType(contentType) {
        return contentType && _.find(MEDIA_TYPES, function(mediaType) {
            return contentType.indexOf(mediaType) === 0;
        });
    }

    function getFieldValues(fieldData) {
        if (fieldData && fieldData.values.length) {
            return _.map(fieldData.values, fieldTypeParsers[fieldData.type]);
        }

        return [];
    }

    var getFieldValue = _.compose(_.first, getFieldValues);

    // Model representing a document in an HOD text index
    return Backbone.Model.extend({
        url: '../api/public/search/get-document-content',

        defaults: {
            authors: [],
            fields: []
        },

        parse: function(response) {
            if (!response.title) {
                // If there is no title, use the last part of the reference (assuming the reference is a file path)
                // C:\Documents\file.txt -> file.txt
                // /home/user/another-file.txt -> another-file.txt
                var splitReference = response.reference.split(/\/|\\/);
                var lastPart = _.last(splitReference);

                if (/\S/.test(lastPart)) {
                    // Use the "file name" if it contains a non whitespace character
                    response.title = lastPart;
                } else {
                    response.title = response.reference;
                }
            }

            if (response.date) {
                response.date = moment(response.date);
            }

            response.thumbnail = getFieldValue(response.fieldMap.thumbnail);
            response.contentType = getFieldValue(response.fieldMap.contentType);
            response.url = getFieldValue(response.fieldMap.url);
            response.offset = getFieldValue(response.fieldMap.offset);
            response.mmapUrl = getFieldValue(response.fieldMap.mmapUrl);
            response.longitude = getFieldValue(response.fieldMap.longitude);
            response.latitude = getFieldValue(response.fieldMap.latitude);
            response.sourceType = getFieldValue(response.fieldMap.sourceType);
            response.transcript = getFieldValue(response.fieldMap.transcript);

            response.media = getMediaType(response.contentType);

            response.authors = getFieldValues(response.fieldMap.authors);

            response.fields = _.chain(response.fieldMap)
                .map(function(fieldData) {
                    return _.defaults({
                        values: getFieldValues(fieldData)
                    }, fieldData);
                })
                .sortBy('displayName')
                .value();

            delete response.fieldMap;
            return response;
        },

        isMedia: function() {
            return Boolean(this.get('media') && this.get('url'));
        },

        isWebType: function() {
            var contentType = this.get('contentType');

            return contentType && _.contains(WEB_TYPES, contentType.toLowerCase());
        }
    });

});
