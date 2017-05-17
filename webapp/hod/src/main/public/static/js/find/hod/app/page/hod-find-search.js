/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/page/find-search',
    'find/hod/app/model/hod-indexes-collection',
    'find/hod/app/page/search/hod-service-view',
    'find/hod/app/page/search/suggest/hod-suggest-view',
    'find/app/util/database-name-resolver',
    'find/hod/app/page/search/hod-query-left-side-view'
], function(_, FindSearch, IndexesCollection, ServiceView, SuggestView,
            databaseNameResolver, HodQueryLeftSideView) {
    'use strict';

    return FindSearch.extend({
        IndexesCollection: IndexesCollection,
        ServiceView: ServiceView,
        SuggestView: SuggestView,
        QueryLeftSideView: HodQueryLeftSideView,

        getSearchTypes: function() {
            return {
                QUERY: _.defaults({
                    // HOD doesn't support parametric numeric date ranges yet
                    showTimeBar: false
                }, FindSearch.prototype.getSearchTypes.call(this).QUERY)
            };
        },

        documentDetailOptions: function(domain, index, reference) {
            return {
                database: databaseNameResolver.constructDatabaseString(domain, index),
                reference: reference
            };
        },

        suggestOptions: function(domain, index, reference) {
            const database = databaseNameResolver.constructDatabaseString(domain, index);

            return {
                database: database,
                reference: reference,
                suggestParams: {
                    indexes: [database]
                }
            };
        }
    });
});
