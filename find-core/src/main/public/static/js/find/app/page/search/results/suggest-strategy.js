/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
], function () {
    "use strict";

    function displayPromotions() {
        return false;
    }

    function requestParams(queryModel) {
        return {
            reference: queryModel.get('document').get('reference')
        };
    }

    function validateQuery(queryModel) {
        return queryModel.get('document');
    }

    function waitForIndexes() {
        return false;
    }

    return {
        colourboxGrouping: 'similar-document-results',
        displayPromotions: displayPromotions,
        requestParams: requestParams,
        validateQuery: validateQuery,
        waitForIndexes: waitForIndexes
    }
});
