/*
 * Copyright 2018 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
