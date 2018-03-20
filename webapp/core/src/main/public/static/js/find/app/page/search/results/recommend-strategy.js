/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore'
], function(_) {
    'use strict';

    return {
        answers: _.constant(false),
        promotions: _.constant(false),

        queryModelAttributes: [
            'indexes'
        ],

        waitForIndexes: function(queryModel) {
            return _.isEmpty(queryModel.get('indexes'));
        },

        requestParams: function(queryModel) {
            return {
                indexes: queryModel.get('indexes'),
                summary: 'context',
                maxResultsPerProfile: 2,
                maxTerms: 30,
                maxProfiles: 3
            };
        },

        validateQuery: function(queryModel) {
            return !_.isEmpty(queryModel.get('indexes'));
        }
    };
});
