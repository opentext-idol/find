/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/page/find-search',
    'find/hod/app/page/search/hod-service-view',
    'find/hod/app/page/search/saved-searches/comparison/hod-comparison-view',
    'find/hod/app/page/search/suggest/hod-suggest-view',
    'find/app/util/database-name-resolver'
], function(_, FindSearch, ServiceView, ComparisonView, SuggestView, databaseNameResolver) {
    'use strict';

    return FindSearch.extend({
        ServiceView: ServiceView,
        ComparisonView: ComparisonView,
        SuggestView: SuggestView,

        documentDetailOptions: function (domain, index, reference) {
            return {
                reference: reference,
                database: databaseNameResolver.constructDatabaseString(domain, index)
            };
        },

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
