/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'moment'
], function(Backbone, _, moment) {
    "use strict";

    // Model representing a document in an HOD text index
    return Backbone.Model.extend({
        parse: function(response) {
            if (response.date) {
                response.date = moment(response.date);
            }

            response.fields = _.map(response.fieldMap, function (value) {
                if (value.type === 'DATE') {
                    value.values = _.map(value.values, function(value) {
                        return moment(value).format('LLLL');
                    })
                }

                return value;
            });

            response.fieldMap = null;

            _.sortBy(response.fields, 'displayName');

            return response;
        }
    });

});
