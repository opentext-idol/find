/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([], function () {
    "use strict";

    function displayPromotions() {
        return true;
    }

    function requestParams(queryModel, infiniteScroll) {
        return {
            text: queryModel.get('queryText'),
            auto_correct: false
        };
    }

    function validateQuery(queryModel) {
        return queryModel.get('queryText');
    }

    function waitForIndexes(queryModel) {
        return _.isEmpty(queryModel.get('indexes'));
    }

    return {
        colourboxGrouping: 'results',
        displayPromotions: displayPromotions,
        requestParams: requestParams,
        validateQuery: validateQuery,
        waitForIndexes: waitForIndexes
    }
});