/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore'
], function(_) {
    'use strict';

    return {
        promotions: _.constant(false),

        queryModelAttributes: [
            'indexes',
            'reference'
        ],

        waitForIndexes: function(queryModel) {
            return _.isEmpty(queryModel.get('indexes'));
        },

        requestParams: function(queryModel) {
            return {
                indexes: queryModel.get('indexes'),
                reference: queryModel.get('reference'),
                summary: 'concept'
            };
        },

        validateQuery: function(queryModel) {
            return Boolean(queryModel.get('reference'));
        }
    };
});
