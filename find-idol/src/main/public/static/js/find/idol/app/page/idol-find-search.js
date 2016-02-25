/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/find-search',
    'find/idol/app/page/search/idol-service-view',
    'find/idol/app/page/search/saved-searches/comparison/idol-comparison-view'
], function(FindSearch, ServiceView, ComparisonView) {
    'use strict';

    return FindSearch.extend({
        ServiceView: ServiceView,
        ComparisonView: ComparisonView,

        documentDetailOptions: function (database, reference) {
            return {
                reference: reference,
                database: database
            };
        }
    });
});
