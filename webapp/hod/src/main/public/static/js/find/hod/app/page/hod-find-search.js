/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
