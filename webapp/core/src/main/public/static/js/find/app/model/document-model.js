/*
 * Copyright 2014-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'moment',
    'find/app/configuration',
    'idol-wkt/js/parser'
], function(Backbone, _, moment, configuration, idolWktParser) {
    
    'use strict';

    const MEDIA_TYPES = ['audio', 'image', 'video'];
    const WEB_TYPES = ['text/html', 'text/xhtml'];
    const isUrlRegex = /^(?:https?|ftp):\/\/|\\\\\S+/;

    function isURL(reference) {
        return isUrlRegex.test(reference);
    }

    let patterns = null;

    function getPreviewWhitelistPatterns() {
        if (!patterns) {
            patterns = [];

            const config = configuration();
            if (config && config.uiCustomization && config.uiCustomization.previewWhitelistUrls) {

                _.each(config.uiCustomization.previewWhitelistUrls, function(value, key){
                    patterns.push({
                        regex: new RegExp(key, 'i'),
                        template: _.template(value)
                    })
                })
            }
        }

        return patterns;
    }

    const fieldTypeParsers = {
        STRING: function (valueWrapper) {
            return valueWrapper.displayValue
        },
        NUMBER: function (valueWrapper) {
            return valueWrapper.value
        },
        GEOINDEX: function (valueWrapper) {
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
        },
        RECORD: function (valueWrapper) {
            return valueWrapper.value;
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
                    .map(function (field) {
                        let locations = [];

                        if (field.geoindexField) {
                            const wellKnownText = getFieldValues(response.fieldMap[field.geoindexField]);

                            _.each(wellKnownText, function(text){
                                try {
                                    const parsed = idolWktParser.parse(text);
                                    if (parsed.type === 'POINT') {
                                        locations.push({
                                            latitude: parsed.point[0],
                                            longitude: parsed.point[1]
                                        });
                                    }
                                    else if (parsed.type === 'POLYGON') {
                                        locations.push({
                                            polygon: parsed.polygon
                                        });
                                    }
                                } catch (e) {
                                    // this is not a valid point, ignore it
                                }
                            });
                        }
                        else {
                            const latitudes = getFieldValues(response.fieldMap[field.latitudeField]);
                            const longitudes = getFieldValues(response.fieldMap[field.longitudeField]);

                            _.each(_.zip(latitudes, longitudes), function(coordinates){
                                locations.push({
                                    latitude: coordinates[0],
                                    longitude: coordinates[1]
                                });
                            });
                        }

                        return locations.map(function (info) {
                            return _.extend(info, {
                                displayName: field.displayName,
                                iconName: field.iconName,
                                iconColor: field.iconColor,
                                markerColor: field.markerColor
                            });
                        });
                    })
                    .flatten()
                    .groupBy('displayName')
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

        getPreviewTemplate: function() {
            const reference = this.get('reference');

            if (reference) {
                const patterns = getPreviewWhitelistPatterns();

                for (let ii = 0; ii < patterns.length; ++ii) {
                    const pattern = patterns[ii];
                    const match = pattern.regex.exec(reference);
                    if (match) {
                        return pattern.template(_.extend({
                            match: match
                        }, this.attributes));
                    }
                }
            }

            return null;
        },

        isWebType: function() {
            const contentType = this.get('contentType');

            return contentType && _.contains(WEB_TYPES, contentType.toLowerCase());
        }
    });

});
