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

    // Model representing a document in an HOD text index
    return Backbone.Model.extend({
        defaults: _.reduce(ARRAY_FIELDS, function(memo, fieldName) {
            memo[fieldName] = [];
            return memo;
        }, {}),

        parse: function(response) {
            _.each(DATE_FIELDS, function(fieldName) {
                if (response[fieldName]) {
                    response[fieldName] = moment(response[fieldName]);
                }
            });

            return response;
        }
    }, {
        ARRAY_FIELDS: ARRAY_FIELDS,
        DATE_FIELDS: DATE_FIELDS
    });

});
