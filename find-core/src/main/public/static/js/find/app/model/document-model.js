/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'moment'
], function(Backbone, _, moment) {

    var ARRAY_FIELDS = ['authors', 'categories'];
    var DATE_FIELDS = ['date', 'dateCreated', 'dateModified'];

    var MEDIA_TYPES = ['audio', 'video'];
    var WEB_TYPES = ['text/html', 'text/xhtml'];

    // Model representing a document in an HOD text index
    return Backbone.Model.extend({
        url: '../api/public/search/get-document-content',

        defaults: _.reduce(ARRAY_FIELDS, function(memo, fieldName) {
            memo[fieldName] = [];
            return memo;
        }, {}),

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

            _.each(DATE_FIELDS, function(fieldName) {
                if (response[fieldName]) {
                    response[fieldName] = moment(response[fieldName]);
                }
            });

            return response;
        },

        isMedia: function() {
            return !!(this.getMediaType() && this.get('url'));
        },

        getMediaType: function() {
            var contentType = this.get('contentType');

            return contentType && _.find(MEDIA_TYPES, function (mediaType) {
                return contentType.indexOf(mediaType) === 0;
            });
        },

        isWebType: function() {
            var contentType = this.get('contentType');

            return contentType && _.contains(WEB_TYPES, contentType.toLowerCase());
        }
    }, {
        ARRAY_FIELDS: ARRAY_FIELDS,
        DATE_FIELDS: DATE_FIELDS
    });

});
