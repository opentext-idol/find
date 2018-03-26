/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/configuration'
], function(_, configuration) {
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
            const config = configuration();
            const profile = config.uiCustomization.profile;
            return {
                indexes: queryModel.get('indexes'),
                summary: 'context',
                maxResultsPerProfile: profile.maxResultsPerProfile,
                maxTerms: profile.maxTerms,
                maxProfiles: profile.maxProfiles,
                highlight: profile.highlightTerms || false
            };
        },

        validateQuery: function(queryModel) {
            return !_.isEmpty(queryModel.get('indexes'));
        }
    };
});
