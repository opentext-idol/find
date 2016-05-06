/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define(['underscore'], function(_) {

    return {
        waitForIndexes: _.constant(false),

        promotions: _.constant(false),

        requestParams: function(queryModel) {
            return {
                text: queryModel.get('queryText'),
                state_match_ids: queryModel.get('stateMatchIds'),
                state_dont_match_ids: queryModel.get('stateDontMatchIds'),
                summary: 'context'
            };
        },

        validateQuery: function(queryModel) {
            return !_.isEmpty(queryModel.get('stateMatchIds'));
        }
    };

});
