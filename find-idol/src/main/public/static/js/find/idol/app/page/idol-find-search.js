/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/find-search',
    'underscore',
    'i18n!find/nls/bundle',
    'find/app/model/saved-searches/saved-search-model',
    'find/idol/app/page/search/idol-service-view',
    'find/idol/app/page/search/suggest/idol-suggest-view',
    'find/idol/app/page/search/saved-searches/comparison/idol-comparison-view',
    'find/app/page/search/snapshots/snapshot-data-view',
    'find/app/page/search/results/state-token-strategy',
    'find/app/model/comparisons/comparison-documents-collection',
    'find/app/page/search/related-concepts/related-concepts-click-handlers',
    'find/idol/app/page/search/idol-query-left-side-view'
], function(FindSearch, _, i18n, SavedSearchModel, ServiceView, SuggestView, ComparisonView, SnapshotDataView, stateTokenStrategy,
            ComparisonDocumentsCollection, relatedConceptsClickHandlers, IdolQueryLeftSideView) {

    'use strict';

    return FindSearch.extend({
        ServiceView: ServiceView,
        ComparisonView: ComparisonView,
        SuggestView: SuggestView,
        QueryLeftSideView: IdolQueryLeftSideView,

        getSearchTypes: function() {
            return _.extend({
                SNAPSHOT: {
                    autoCorrect: false,
                    queryTextModelChange: _.constant(_.noop),
                    collection: 'savedSnapshotCollection',
                    isMutable: false,
                    fetchStrategy: stateTokenStrategy,
                    // TODO: Display promotions when QMS supports state tokens
                    displayPromotions: false,
                    DocumentsCollection: ComparisonDocumentsCollection,
                    LeftSideFooterView: SnapshotDataView,
                    MiddleColumnHeaderView: null,
                    relatedConceptsClickHandler: relatedConceptsClickHandlers.newQuery,
                    createSearchModelAttributes: function() {
                        return {inputText: '', relatedConcepts: []};
                    },
                    searchModelChange: function(options) {
                        return function() {
                            var newSearch = new SavedSearchModel({
                                queryText: options.searchModel.get('inputText'),
                                relatedConcepts: [],
                                title: i18n['search.newSearch'],
                                type: SavedSearchModel.Type.QUERY
                            });

                            options.savedQueryCollection.add(newSearch);
                            options.selectedTabModel.set('selectedSearchCid', newSearch.cid);
                        };
                    },
                    topicMapClickHandler: function(options) {
                        return function(text) {
                            var newQuery = new SavedSearchModel(_.defaults({
                                id: null,
                                queryText: text,
                                title: i18n['search.newSearch'],
                                type: SavedSearchModel.Type.QUERY
                            }, options.savedSearchModel.attributes));

                            options.savedQueryCollection.add(newQuery);
                            options.selectedTabModel.set('selectedSearchCid', newQuery.cid);
                        };
                    }
                }
            }, FindSearch.prototype.getSearchTypes.call(this));
        },

        documentDetailOptions: function (database, reference) {
            return {
                reference: reference,
                database: database
            };
        },

        suggestOptions: function (database, reference) {
            return {
                database: database,
                reference: reference
            };
        }
    });
});
