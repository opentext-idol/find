/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'moment',
    'find/app/configuration'
], function(Backbone, _, moment, configuration) {
    
    'use strict';

    const MEDIA_TYPES = ['audio', 'image', 'video'];
    const WEB_TYPES = ['text/html', 'text/xhtml'];
    const isUrlRegex = /^(?:https?|ftp):\/\/|\\\\\S+/;

    function isURL(reference) {
        return isUrlRegex.test(reference);
    }

    const fieldTypeParsers = {
        STRING: function (valueWrapper) {
            return valueWrapper.displayValue
        },
        NUMBER: function (valueWrapper) {
            return valueWrapper.value
        },
        /**
         * @return {boolean}
         */
        BOOLEAN: function (valueWrapper) {
            return valueWrapper.value.toLowerCase() === 'true';
        },
        DATE: function (valueWrapper) {
            return moment(valueWrapper.value).format('LLLL');
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

    const getFieldValue = _.compose(_.first, getFieldValues);

    // Model representing a document in an HOD text index
    return Backbone.Model.extend({
        url: 'api/public/search/get-document-content',

        defaults: {
            authors: [],
            fields: []
        },

        parse: function(response) {
            if (!response.title) {
                // If there is no title, use the last part of the reference (assuming the reference is a file path)
                // C:\Documents\file.txt -> file.txt
                // /home/user/another-file.txt -> another-file.txt
                const splitReference = response.reference.split(/\/|\\/);
                const lastPart = _.last(splitReference);

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
            response.thumbnailUrl = getFieldValue(response.fieldMap.thumbnailUrl);
            response.contentType = getFieldValue(response.fieldMap.contentType);            
            response.offset = getFieldValue(response.fieldMap.offset);
            response.mmapEventSourceType = getFieldValue(response.fieldMap.mmapEventSourceType);
            response.mmapEventSourceName = getFieldValue(response.fieldMap.mmapEventSourceName);
            response.mmapEventTime = getFieldValue(response.fieldMap.mmapEventTime);
            response.mmapUrl = getFieldValue(response.fieldMap.mmapUrl);
            response.sourceType = getFieldValue(response.fieldMap.sourceType);
            response.transcript = getFieldValue(response.fieldMap.transcript);

            response.url = getFieldValue(response.fieldMap.url) || (isURL(response.reference) ? response.reference : '');
            
            if (configuration().map.enabled) {
                response.locations = _.chain(configuration().map.locationFields)
                    .filter(function (field) {
                        const latitude = getFieldValue(response.fieldMap[field.latitudeField]);
                        const longitude = getFieldValue(response.fieldMap[field.longitudeField]);
                        return longitude && latitude;
                    })
                    .map(function (field) {
                        const latitude = getFieldValue(response.fieldMap[field.latitudeField]);
                        const longitude = getFieldValue(response.fieldMap[field.longitudeField]);

                        return {
                            displayName: field.displayName,
                            latitude: latitude,
                            longitude: longitude
                        }
                    })
                    .value()
            }

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
            const contentType = this.get('contentType');

            return contentType && _.contains(WEB_TYPES, contentType.toLowerCase());
        }
    });

});
