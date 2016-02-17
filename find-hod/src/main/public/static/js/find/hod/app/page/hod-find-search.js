/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'find/app/model/query-model',
    'find/app/page/find-search',
    'find/app/util/database-name-resolver',
    'find/hod/app/page/search/hod-query-service-view',
    'find/hod/app/page/search/hod-suggest-service-view'
], function ($, QueryModel, FindSearch, databaseNameResolver, QueryServiceView, SuggestServiceView) {
    'use strict';

    return FindSearch.extend({
        QueryServiceView: QueryServiceView,
        SuggestServiceView: SuggestServiceView,

        suggestOptions: function (domain, index, reference) {
            var database = databaseNameResolver.constructDatabaseString(domain, index);
            return {
                reference: reference,
                database: database,
                suggestParams: {
                    indexes: [database]
                }
            };
        }
    });
});
