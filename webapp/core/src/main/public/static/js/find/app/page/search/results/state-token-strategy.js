/*
 * Copyright 2016 Open Text.
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

define(['underscore'], function(_) {

    'use strict';

    const baseParams = function(queryModel) {
        return {
            text: queryModel.get('queryText'),
            summary: 'context'
        }
    };

    return {
        waitForIndexes: _.constant(false),
        answers: _.constant(false),

        queryModelAttributes: [
            'stateMatchIds',
            'stateDontMatchIds',
            'promotionsStateMatchIds'
        ],

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
