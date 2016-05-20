/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define(['underscore'], function(_) {

    var baseParams = function(queryModel) {
        return {
            text: queryModel.get('queryText'),
            summary: 'context'
        }
    };
    
    return {
        waitForIndexes: _.constant(false),

        // Query for promotions only if the snapshot has a promotions state token.
        // Necessary to accommodate legacy snapshots predating QMS integration (FIND-30).
        promotions: function(queryModel) {
            if (queryModel.get('promotionsStateMatchIds')) {
                return queryModel.get('promotionsStateMatchIds').length > 0;
            } else {
                return false;
            }
        },

        requestParams: function(queryModel) {
            return _.extend(baseParams(queryModel), {
                state_match_ids: queryModel.get('stateMatchIds'),
                state_dont_match_ids: queryModel.get('stateDontMatchIds')
            });
        },

        promotionsRequestParams: function(queryModel) {
            return _.extend(baseParams(queryModel), {
                state_match_ids: queryModel.get('promotionsStateMatchIds')
            });
        },

        validateQuery: function(queryModel) {
            return !_.isEmpty(queryModel.get('stateMatchIds'));
        }
    };

});
