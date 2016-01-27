/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'find/app/model/query-model',
    'find/app/page/find-search',
    'find/idol/app/page/search/idol-query-service-view',
    'find/idol/app/page/search/idol-suggest-service-view'
], function($, QueryModel, FindSearch, QueryServiceView, SuggestServiceView) {
    'use strict';

    return FindSearch.extend({
        QueryServiceView: QueryServiceView,
        SuggestServiceView: SuggestServiceView,

        suggestOptions: function (database, reference) {
            return {
                database: database,
                reference: reference
            };
        }
    });
});
