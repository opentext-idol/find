/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/page/find-search',
    'i18n!find/nls/bundle',
    'find/idol/nls/root/snapshots',
    'find/app/model/saved-searches/saved-search-model',
    'find/idol/app/model/idol-indexes-collection',
    'find/idol/app/page/search/idol-service-view',
    'find/idol/app/page/search/suggest/idol-suggest-view',
    'find/idol/app/page/search/snapshots/snapshot-data-view',
    'find/idol/app/page/search/comparison/comparison-view',
    'find/app/page/search/results/state-token-strategy',
    'find/app/page/search/results/query-strategy',
    'find/idol/app/model/comparison/comparison-documents-collection',
    'find/app/model/documents-collection',
    'find/app/page/search/related-concepts/related-concepts-click-handlers',
    'find/idol/app/page/search/idol-query-left-side-view',
    'find/idol/app/page/search/comparison/compare-modal',
    'find/app/configuration'
], function(_, FindSearch, i18n, snapshotsI18n, SavedSearchModel, IndexesCollection, ServiceView, SuggestView,
            SnapshotDataView, ComparisonView, stateTokenStrategy, queryStrategy, ComparisonDocumentsCollection,
            DocumentsCollection, relatedConceptsClickHandlers, IdolQueryLeftSideView, CompareModal, configuration) {
    'use strict';

    return FindSearch.extend({
        IndexesCollection: IndexesCollection,
        ServiceView: ServiceView,
        SuggestView: SuggestView,
        QueryLeftSideView: IdolQueryLeftSideView,

        getSearchTypes: function() {
            return _.extend(FindSearch.prototype.getSearchTypes.call(this),
                configuration().hasBiRole
                    ? {
                        SNAPSHOT: {
                            cssClass: 'snapshot',
                            autoCorrect: false,
                            queryTextModelChange: _.constant(_.noop),
                            collection: 'savedSnapshotCollection',
                            icon: 'hp-camera',
                            isMutable: false,
                            fetchStrategy: stateTokenStrategy,
                            showTimeBar: false,
                            DocumentsCollection: ComparisonDocumentsCollection,
                            LeftSideFooterView: SnapshotDataView,
                            MiddleColumnHeaderView: null,
                            relatedConceptsClickHandler: relatedConceptsClickHandlers.newQuery,
                            openEditText: {
                                create: snapshotsI18n['openEdit.create'],
                                edit: snapshotsI18n['openEdit.edit']
                            }
                        },
                        READ_ONLY: {
                            cssClass: 'readonly',
                            autoCorrect: false,
                            queryTextModelChange: _.constant(_.noop),
                            collection: 'readOnlySearchCollection',
                            icon: 'hp-dashboard',
                            isMutable: false,
                            fetchStrategy: queryStrategy,
                            showTimeBar: false,
                            DocumentsCollection: DocumentsCollection,
                            LeftSideFooterView: SnapshotDataView,
                            MiddleColumnHeaderView: null,
                            relatedConceptsClickHandler: relatedConceptsClickHandlers.newQuery,
                            openEditText: {
                                create: snapshotsI18n['openEdit.create'],
                                edit: snapshotsI18n['openEdit.edit']
                            }
                        },
                        SHARED_QUERY: {
                            cssClass: 'query',
                            autoCorrect: false,
                            collection: 'sharedSavedQueryCollection',
                            icon: 'hp-search',
                            isMutable: true,
                            fetchStrategy: queryStrategy,
                            showTimeBar: false,
                            DocumentsCollection: DocumentsCollection,
                            LeftSideFooterView: this.QueryLeftSideView,
                            MiddleColumnHeaderView: null,
                            relatedConceptsClickHandler: relatedConceptsClickHandlers.newQuery,
                            isShared: true,
                            openEditText: {
                                create: snapshotsI18n['openEdit.create'],
                                edit: snapshotsI18n['openEdit.edit']
                            }
                        },
                        SHARED_READ_ONLY_QUERY: {
                            cssClass: 'query',
                            autoCorrect: false,
                            collection: 'sharedSavedQueryCollection',
                            icon: 'hp-search',
                            isMutable: false,
                            fetchStrategy: queryStrategy,
                            showTimeBar: false,
                            DocumentsCollection: DocumentsCollection,
                            LeftSideFooterView: SnapshotDataView,
                            MiddleColumnHeaderView: null,
                            relatedConceptsClickHandler: relatedConceptsClickHandlers.newQuery,
                            isShared: true,
                            openEditText: {
                                create: snapshotsI18n['openEdit.create'],
                                edit: snapshotsI18n['openEdit.edit']
                            }
                        },
                        SHARED_SNAPSHOT: {
                            cssClass: 'snapshot',
                            autoCorrect: false,
                            queryTextModelChange: _.constant(_.noop),
                            collection: 'sharedSavedSnapshotCollection',
                            icon: 'hp-camera',
                            isMutable: false,
                            fetchStrategy: stateTokenStrategy,
                            showTimeBar: true,
                            DocumentsCollection: ComparisonDocumentsCollection,
                            LeftSideFooterView: SnapshotDataView,
                            MiddleColumnHeaderView: null,
                            relatedConceptsClickHandler: relatedConceptsClickHandlers.newQuery,
                            isShared: true,
                            openEditText: {
                                create: snapshotsI18n['openEdit.create'],
                                edit: snapshotsI18n['openEdit.edit']
                            }
                        },
                        SHARED_READ_ONLY_SNAPSHOT: {
                            cssClass: 'snapshot',
                            autoCorrect: false,
                            queryTextModelChange: _.constant(_.noop),
                            collection: 'sharedSavedSnapshotCollection',
                            icon: 'hp-camera',
                            isMutable: false,
                            fetchStrategy: stateTokenStrategy,
                            showTimeBar: false,
                            DocumentsCollection: ComparisonDocumentsCollection,
                            LeftSideFooterView: SnapshotDataView,
                            MiddleColumnHeaderView: null,
                            relatedConceptsClickHandler: relatedConceptsClickHandlers.newQuery,
                            isShared: true,
                            openEditText: {
                                create: snapshotsI18n['openEdit.create'],
                                edit: snapshotsI18n['openEdit.edit']
                            }
                        }
                    }
                    : {});
        },

        serviceViewOptions: function(cid) {
            return {
                comparisonModalCallback: function() {
                    new CompareModal({
                        cid: cid,
                        savedSearchCollection: this.savedSearchCollection,
                        queryStates: this.queryStates,
                        comparisonSuccessCallback: function(model, searchModels) {
                            this.removeComparisonView();

                            this.$('.service-view-container').addClass('hide');
                            this.$('.comparison-service-view-container').removeClass('hide');

                            this.comparisonView = new ComparisonView({
                                model: model,
                                searchModels: searchModels,
                                scrollModel: this.windowScrollModel,
                                escapeCallback: function() {
                                    this.removeComparisonView();
                                    this.$('.service-view-container').addClass('hide');
                                    this.$('.query-service-view-container').removeClass('hide');
                                }.bind(this)
                            });

                            this.comparisonView.$el.insertBefore(this.$('.hp-logo-footer'));
                            this.comparisonView.render();
                        }.bind(this)
                    });
                }.bind(this)
            };
        },

        documentDetailOptions: function(database, reference) {
            return {
                database: database,
                reference: reference
            };
        },

        suggestOptions: function(database, reference) {
            return {
                database: database,
                reference: reference
            };
        },

        removeComparisonView: function() {
            if(this.comparisonView) {
                // Setting the element to nothing prevents the containing element from being
                // removed when the view is removed
                this.comparisonView.setElement();
                this.comparisonView.remove();
                this.stopListening(this.comparisonView);
                this.comparisonView = null;
            }
        },

        remove: function() {
            this.removeComparisonView();
            FindSearch.prototype.remove.call(this);
        }
    });
});
