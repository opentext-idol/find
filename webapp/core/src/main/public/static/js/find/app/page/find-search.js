/*
 * Copyright 2014-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'js-whatever/js/base-page',
    'find/app/configuration',
    'find/app/model/dates-filter-model',
    'find/app/model/geography-model',
    'find/app/model/document-selection-model',
    './search/document-renderer',
    'parametric-refinement/selected-values-collection',
    'find/app/model/documents-collection',
    'find/app/page/search/input-view',
    'find/app/page/search/input-view-query-text-strategy',
    'find/app/page/search/tabbed-search-view',
    'find/app/util/merge-collection',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/page/search/query-middle-column-header-view',
    'find/app/model/min-score-model',
    'find/app/model/query-text-model',
    'find/app/model/document-model',
    'find/app/page/search/document-content-view',
    'find/app/page/search/document/document-detail-content-view',
    'find/app/page/search/results/query-strategy',
    'find/app/page/search/related-concepts/related-concepts-click-handlers',
    'find/app/util/database-name-resolver',
    'find/app/util/saved-query-result-poller',
    'find/app/util/events',
    'find/app/router',
    'find/app/vent',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/find-search.html'
], function(_, $, Backbone, BasePage, config, DatesFilterModel, GeographyModel, DocumentSelectionModel, DocumentRenderer, SelectedParametricValuesCollection,
            DocumentsCollection, InputView, queryTextStrategy, TabbedSearchView, MergeCollection,
            SavedSearchModel, QueryMiddleColumnHeaderView, MinScoreModel, QueryTextModel, DocumentModel,
            DocumentContentView, DocumentDetailContentView, queryStrategy, relatedConceptsClickHandlers, databaseNameResolver,
            SavedQueryResultPoller, events, router, vent, i18n, template) {
    'use strict';

    const reducedClasses = 'reverse-animated-container col-md-offset-1 ' +
        'col-lg-offset-2 col-xs-12 col-sm-12 col-md-10 col-lg-8';
    const expandedClasses = 'animated-container col-sm-offset-0 col-md-offset-3 ' +
        'col-lg-offset-3 col-md-6 col-lg-6 col-xs-9 col-sm-9';

    const html = _.template(template)({i18n: i18n});

    const configuration = config();
    const defaultDeselectedDatabases = _.map(configuration && configuration.uiCustomization &&
        configuration.uiCustomization.defaultDeselectedDatabases || []);

    const dbSelectMap = _.reduce(defaultDeselectedDatabases, function(acc, val){
        acc[val.toLowerCase()] = false;
        return acc;
    }, {})

    function selectInitialIndexes(indexesCollection) {
        const privateIndexes = indexesCollection.reject({domain: 'PUBLIC_INDEXES'});
        const selectedIndexes = privateIndexes.length > 0
            ? privateIndexes
            : indexesCollection.models;

        return _.map(selectedIndexes, function(indexModel) {
            return indexModel.pick('domain', 'name');
        });
    }

    function selectFilteredInitialIndexes(indexesCollection) {
        const active = selectInitialIndexes(indexesCollection);

        return defaultDeselectedDatabases.length ? _.filter(active, function(index){
            return !_.has(dbSelectMap, index.name.toLowerCase());
        }) : active;
    }

    return BasePage.extend({
        className: 'search-page',
        template: _.template(template),

        // Callbacks to bind the search bar to the active tab; will be removed and added as the user changes tabs
        searchChangeCallback: null,
        queryTextCallback: null,

        // May be overridden
        QueryMiddleColumnHeaderView: QueryMiddleColumnHeaderView,
        serviceViewOptions: _.constant({}),

        // Abstract
        IndexesCollection: null,
        ServiceView: null,
        SuggestView: null,
        documentDetailOptions: null,
        suggestOptions: null,
        QueryLeftSideView: null,

        initialize: function(options) {
            this.configuration = config();
            this.mmapTab = options.mmapTab;
            const optionalViews = [{
                enabled: !this.configuration.hasBiRole,
                selector: '.input-view-container',
                construct: function() {
                    // Model representing search bar text
                    this.searchModel = new QueryTextModel();

                    this.listenTo(this.searchModel, 'change', function() {
                        // Bind search model to routing
                        vent.navigate(this.generateURL(), {trigger: false});

                        if(this.searchModel.get('inputText')) {
                            this.toggleExpandedState(true);

                            // Create a tab if the user has run a search but has no open tabs
                            if(this.selectedTabModel.get('selectedSearchCid') === null) {
                                this.createNewTab(this.searchModel.get('inputText'));
                            }
                        }
                    });

                    return new InputView({
                        enableTypeAhead: this.configuration.enableTypeAhead,
                        strategy: queryTextStrategy(this.searchModel)
                    });
                }.bind(this),
                onExpand: function(instance) {
                    instance.unFocus();
                },
                onReduce: function(instance) {
                    instance.focus();
                }
            }];

            this.optionalViews = _.where(optionalViews, {enabled: true});

            this.savedQueryCollection = options.savedQueryCollection;
            this.sharedSavedQueryCollection = options.sharedSavedQueryCollection;
            this.indexesCollection = options.indexesCollection;
            this.windowScrollModel = options.windowScrollModel;

            this.searchTypes = this.getSearchTypes();

            this.searchCollections = _.mapObject(this.searchTypes, function(data) {
                return options[data.collection];
            });

            // determine wich collections are shared and which are unshared
            // searchCollections[0] is the shared collections, and [1] is unshared
            const searchCollections = _.chain(this.searchTypes)
                .pairs()
                .partition(function(pair) {
                    return pair[1].isShared;
                })
                .map(function(pairs) {
                    return _.map(pairs, function(pair) {
                        return this.searchCollections[pair[0]];
                    }, this)
                }, this)
                .value();


            this.savedSearchCollection = new MergeCollection([], {
                collections: searchCollections[1]
            });

            this.sharedSavedSearchCollection = new MergeCollection([], {
                collections: searchCollections[0]
            });

            this.selectedTabModel = new Backbone.Model({
                selectedSearchCid: null
            });

            this.documentRenderer = new DocumentRenderer(this.configuration.templatesConfig);

            // Model mapping saved search cids to query state
            this.queryStates = new Backbone.Model();

            // Map of saved search cid to ServiceView
            this.serviceViews = {};

            this.listenTo(this.selectedTabModel, 'change', this.selectContentView);

            this.listenTo(this.savedSearchCollection, 'remove', function(savedSearch) {
                const cid = savedSearch.cid;
                this.serviceViews[cid].view.remove();
                this.queryStates.unset(cid);
                delete this.serviceViews[cid];

                events(cid).abandon();

                if(this.selectedTabModel.get('selectedSearchCid') === cid) {
                    const lastModel = this.savedQueryCollection.last();

                    if(lastModel) {
                        const route = lastModel.get('id') ? 'search/tab/' + lastModel.get('type') + ':' + lastModel.get('id') : 'search/query';
                        vent.navigate(route, {trigger: false});
                        this.selectedTabModel.set('selectedSearchCid', lastModel.cid);
                    } else {
                        // If the user closes their last tab, run a search for *
                        this.createNewTab();
                    }
                }
            });

            this.optionalViews.forEach(function(view) {
                view.instance = view.construct();
            });

            if(config().hasBiRole) {
                this.tabView = new TabbedSearchView({
                    savedSearchCollection: this.savedSearchCollection,
                    sharedSavedSearchCollection: this.sharedSavedSearchCollection,
                    model: this.selectedTabModel,
                    queryStates: this.queryStates,
                    searchTypes: this.searchTypes
                });

                this.listenTo(this.tabView, 'startNewSearch', this.createNewTab);

                const savedSearchConfig = config().savedSearchConfig;
                if(savedSearchConfig.pollForUpdates) {
                    this.listenToOnce(this.savedQueryCollection, 'sync', function() {
                        this.savedQueryResultPoller = new SavedQueryResultPoller({
                            config: savedSearchConfig,
                            savedQueryCollection: this.savedQueryCollection,
                            queryStates: this.queryStates,
                            onSuccess: _.bind(function(savedQueryModelId, newResults) {
                                this.savedQueryCollection.get(savedQueryModelId).set({
                                    newDocuments: newResults
                                });
                            }, this)
                        });
                    });
                }
            }

            this.listenTo(router, 'route:searchSplash', function() {
                this.selectedTabModel.set('selectedSearchCid', null);

                if(this.searchModel) {
                    this.searchModel.set({inputText: ''});
                }

                this.toggleExpandedState(false);
            }, this);

            // Bind routing to search model
            this.listenTo(router, 'route:search', function(databases, text) {
                this.removeDocumentDetailView();
                this.removeSuggestView();

                if(this.searchModel) {
                    this.searchModel.set({
                        inputText: text || ''
                    });
                }

                if(this.isExpanded()) {
                    this.$('.service-view-container').addClass('hide');
                    this.$('.query-service-view-container').removeClass('hide');
                }
            }, this);

            this.listenTo(router, 'route:savedSearch', function(tab, resultsView, others) {
                const split = tab.split(':');
                const type = split[0];
                const id = split[1];
                const extraRouteParams = others ? _.map(others.slice(1).split('/'), decodeURIComponent) : [];

                let collection;
                switch(type) {
                    case SavedSearchModel.Type.QUERY:
                        collection = options.savedQueryCollection;
                        break;
                    case SavedSearchModel.Type.SNAPSHOT:
                        collection = options.savedSnapshotCollection;
                        break;
                    case SavedSearchModel.Type.READ_ONLY_QUERY:
                        collection = options.readOnlySearchCollection;
                        break;
                    case SavedSearchModel.Type.READ_ONLY_SNAPSHOT:
                        collection = options.readOnlySearchCollection;
                        break;
                    case SavedSearchModel.Type.SHARED_QUERY:
                        collection = options.sharedSavedQueryCollection;
                        break;
                    case SavedSearchModel.Type.SHARED_SNAPSHOT:
                        collection = options.sharedSavedSnapshotCollection;
                        break;
                    case SavedSearchModel.Type.SHARED_READ_ONLY_QUERY:
                        collection = options.sharedSavedQueryCollection;
                        break;
                    case SavedSearchModel.Type.SHARED_READ_ONLY_SNAPSHOT:
                        collection = options.sharedSavedSnapshotCollection;
                        break;
                }

                const getModel = function () {
                    return new SavedSearchModel({
                        id: id,
                        type: type
                    });
                };

                const setSelectedTab = _.bind(function () {
                    if (collection.get(id)) {
                        this.selectedTabModel.set({
                            selectedSearchCid: collection.get(id).cid,
                            selectedResultsView: resultsView || '',
                            selectedResultsViewRouteParams: extraRouteParams
                        });
                    } else {
                        const newModel = getModel();
                        newModel.fetch().done(function () {
                            collection = options.readOnlySearchCollection;
                            newModel.set('searchType', newModel.get('type'));
                            newModel.set('type', 'READ_ONLY_' + newModel.get('type'));
                            newModel.set('validForSave', false);
                            collection.add(newModel);
                            this.selectedTabModel.set({
                                selectedSearchCid: collection.get(id).cid,
                                selectedResultsView: resultsView || '',
                                selectedResultsViewRouteParams: extraRouteParams
                            });
                        }.bind(this));
                    }
                }, this);

                if (collection.fetching) {
                    collection.currentRequest.done(function() {
                        setSelectedTab();
                    }.bind(this));
                } else {
                    setSelectedTab();
                }

            }, this);

            this.listenTo(router, 'route:documentDetail', function() {
                const backURL = this.suggestView
                    ? this.generateSuggestURL(this.suggestView.documentModel)
                    : this.generateURL();

                this.toggleExpandedState(true);
                this.$('.service-view-container').addClass('hide');
                this.$('.document-detail-service-view-container').removeClass('hide');

                this.removeDocumentDetailView();

                this.documentDetailView = new DocumentContentView(_.extend({
                    backUrl: backURL,
                    ContentView: DocumentDetailContentView,
                    contentViewOptions: {
                        indexesCollection: this.indexesCollection,
                        documentRenderer: this.documentRenderer,
                        mmapTab: this.mmapTab
                    }
                }, this.documentDetailOptions.apply(this, arguments)));

                this.$('.document-detail-service-view-container').append(this.documentDetailView.$el);
                this.documentDetailView.render();
            }, this);

            this.listenTo(router, 'route:suggest', function() {
                this.toggleExpandedState(true);
                this.$('.service-view-container').addClass('hide');
                this.$('.suggest-service-view-container').removeClass('hide');

                const suggestOptions = this.suggestOptions.apply(this, arguments);

                const indexesCollection = suggestOptions.suggestIndexesCollection || this.indexesCollection;

                this.suggestView = new DocumentContentView(_.extend({
                    backUrl: this.generateURL(),
                    ContentView: this.SuggestView,
                    contentViewOptions: {
                        configuration: config(),
                        documentRenderer: this.documentRenderer,
                        indexesCollection: indexesCollection,
                        mmapTab: this.mmapTab,
                        scrollModel: this.windowScrollModel,
                    }
                }, suggestOptions));

                this.$('.suggest-service-view-container').append(this.suggestView.$el);
                this.suggestView.render();
            }, this);

            this.listenTo(this.savedSearchCollection, 'sync', function() {
                const savedSearchModel = this.savedSearchCollection.get(this.selectedTabModel.get('selectedSearchCid'));
                const selectedResultsView = this.selectedTabModel.get('selectedResultsView');
                this.updateRouting(savedSearchModel, selectedResultsView);
            }.bind(this));
        },

        render: function () {
            this.$el.html(html);

            this.optionalViews.forEach(function (view) {
                view.instance.setElement(this.$(view.selector)).render();
            }, this);

            if (this.tabView) {
                this.tabView.setElement(this.$('.search-tabs-container')).render();
            }

            this.toggleExpandedState(this.selectedTabModel.get('selectedSearchCid') !== null || config().hasBiRole);

            _.each(this.serviceViews, function (data) {
                this.$('.query-service-view-container').append(data.view.$el);
                data.view.render();
            }, this);

            if (config().hasBiRole && this.selectedTabModel.get('selectedSearchCid') === null) {
                this.createNewTab(this.lastNavigatedQueryText, this.lastNavigatedDatabases);
            } else {
                this.selectContentView();
            }
        },

        // Overrides method in BasePage
        update: function () {
            const viewData = this.serviceViews[this.selectedTabModel.get('selectedSearchCid')];

            if (viewData && viewData.view.update) {
                // Inform the service view that it is visible again so (e.g.) the topic map can be re-drawn
                viewData.view.update();
            }
        },

        // Can be overridden
        getSearchTypes: function () {
            return {
                QUERY: {
                    cssClass: 'query',
                    autoCorrect: !config().hasBiRole,
                    collection: 'savedQueryCollection',
                    fetchStrategy: queryStrategy,
                    icon: 'hp-search',
                    isMutable: true,
                    relatedConceptsClickHandler: relatedConceptsClickHandlers.updateQuery,
                    showTimeBar: true,
                    LeftSideFooterView: this.QueryLeftSideView,
                    DocumentsCollection: DocumentsCollection,
                    MiddleColumnHeaderView: this.QueryMiddleColumnHeaderView,
                    openEditText: {
                        create: i18n['search.savedSearchControl.openEdit.create'],
                        edit: i18n['search.savedSearchControl.openEdit.edit']
                    },
                    createSearchModelAttributes: function (conceptGroups) {
                        return {
                            inputString: conceptGroups.length > 0
                                ? conceptGroups.first().get('concepts')[0]
                                : '*'
                        };
                    },
                    searchModelChange: function (options) {
                        return function () {
                            const inputText = options.searchModel.get('inputText');
                            if (inputText && inputText !== '*') {
                                options.queryState.conceptGroups.set([{concepts: [inputText], hidden: true}]);
                            } else {
                                options.queryState.conceptGroups.reset();
                            }
                        };
                    }
                }
            };
        },

        createNewTab: function (queryText, databases) {
            const opts = {
                relatedConcepts: queryText ? [queryText.split('\n')] : [],
                title: i18n['search.newSearch'],
                type: SavedSearchModel.Type.QUERY,
                minScore: config().minScore
            };

            if (databases) {
                opts.indexes = databases;
            }

            const newSearch = new SavedSearchModel(opts);

            this.savedQueryCollection.add(newSearch);
            this.selectedTabModel.set('selectedSearchCid', newSearch.cid);
        },

        selectContentView: function () {
            const cid = this.selectedTabModel.get('selectedSearchCid');

            _.each(this.serviceViews, function (data) {
                data.view.$el.addClass('hide');
            }, this);

            if (this.searchModel && this.searchChangeCallback !== null) {
                this.stopListening(this.searchModel, 'change', this.searchChangeCallback);
                this.searchChangeCallback = null;
            }

            if (cid) {
                let viewData;
                let savedSearchModel = this.savedSearchCollection.get(cid);

                // if not found, it must be a shared search
                if (!savedSearchModel) {
                    savedSearchModel = this.sharedSavedSearchCollection.get(cid);
                }

                const searchType = savedSearchModel.get('type');

                events(cid);

                let creating;

                if (this.serviceViews[cid]) {
                    viewData = this.serviceViews[cid];

                    creating = false;
                } else {
                    const documentsCollection = new this.searchTypes[searchType].DocumentsCollection();
                    const savedSelectedIndexes = savedSearchModel.toSelectedIndexes();
                    const isExistingSavedSearch = savedSearchModel.id;
                    const lastNavigatedDatabases = this.lastNavigatedDatabases;

                    const indexFilterFn = isExistingSavedSearch ? selectInitialIndexes
                        : lastNavigatedDatabases ? function(indexesCollection){
                            const active = selectInitialIndexes(indexesCollection);
                            return _.filter(active, function(index){
                                return _.findWhere(lastNavigatedDatabases, {
                                    name: index.name
                                })
                            });
                        }
                        : selectFilteredInitialIndexes;

                    this.lastNavigatedDatabases = undefined;

                    /**
                     * @type {QueryState}
                     */
                    // also constructed in model/saved-searches/saved-search-model:toQueryModel
                    const queryState = {
                        conceptGroups: new Backbone.Collection(savedSearchModel.toConceptGroups()),
                        minScoreModel: new MinScoreModel({minScore: 0}),
                        datesFilterModel: new DatesFilterModel(savedSearchModel.toDatesFilterModelAttributes()),
                        geographyModel: new GeographyModel(savedSearchModel.toGeographyModelAttributes()),
                        documentSelectionModel: new DocumentSelectionModel(
                            savedSearchModel.toDocumentSelectionModelAttributes()),
                        selectedIndexes: new this.IndexesCollection(
                            savedSelectedIndexes.length === 0
                                ? (this.indexesCollection.isEmpty()
                                    ? []
                                    : indexFilterFn(this.indexesCollection))
                                : savedSelectedIndexes
                        ),
                        selectedParametricValues: new SelectedParametricValuesCollection(savedSearchModel.toSelectedParametricValues())
                    };

                    this.queryStates.set(cid, queryState);

                    viewData = {
                        queryState: queryState,
                        documentsCollection: documentsCollection,
                        view: new this.ServiceView(_.extend({
                            delayedIndexesSelection: indexFilterFn,
                            documentsCollection: documentsCollection,
                            documentRenderer: this.documentRenderer,
                            indexesCollection: this.indexesCollection,
                            queryState: queryState,
                            savedSearchCollection: this.savedSearchCollection,
                            savedSearchModel: savedSearchModel,
                            searchCollections: this.searchCollections,
                            searchTypes: this.searchTypes,
                            selectedTabModel: this.selectedTabModel,
                            mmapTab: this.mmapTab
                        }, this.serviceViewOptions(cid)))
                    };
                    this.serviceViews[cid] = viewData;

                    this.$('.query-service-view-container').append(viewData.view.$el);
                    viewData.view.render();

                    this.listenTo(viewData.view, 'updateRouting', _.bind(this.updateRouting, this, savedSearchModel));

                    creating = true;
                }

                if (this.searchModel) {
                    this.searchModel.set(this.searchTypes[searchType]
                        .createSearchModelAttributes(viewData.queryState.conceptGroups));

                    this.searchChangeCallback = this.searchTypes[searchType]
                        .searchModelChange({
                            savedQueryCollection: this.savedQueryCollection,
                            selectedTabModel: this.selectedTabModel,
                            searchModel: this.searchModel,
                            queryState: viewData.queryState
                        });
                    this.listenTo(this.searchModel, 'change', this.searchChangeCallback);
                }

                viewData.view.$el.removeClass('hide');

                if (!creating && viewData.view.update) {
                    viewData.view.update();
                }

                if (this.selectedTabModel.get('selectedResultsView')) {
                    viewData.view.changeTab(this.selectedTabModel.get('selectedResultsView'), this.selectedTabModel.get('selectedResultsViewRouteParams'));
                    this.selectedTabModel.set('selectedResultsView', '');
                    this.selectedTabModel.set('selectedResultsViewRouteParams', []);
                }

                this.updateRouting(savedSearchModel, viewData.view.getSelectedTab());
            }
        },

        generateURL: function () {
            const inputText = this.searchModel
                ? this.searchModel.get('inputText')
                : null;

            return inputText
                ? 'search/query/' + encodeURIComponent(inputText)
                : (this.selectedTabModel.get('selectedSearchCid') || config().hasBiRole
                    ? 'search/query'
                    : 'search/splash');
        },

        generateSuggestURL: function (model) {
            return 'search/suggest/' + vent.addSuffixForDocument(model);
        },

        // bool == true. expanded state. Run fancy animation from large central search bar to main search page
        // bool == false: reduced state. Set view to initial state (large central search bar)
        toggleExpandedState: function (bool) {
            this.$('.find').toggleClass(expandedClasses, bool).toggleClass(reducedClasses, !bool);
            this.$('.service-view-container').addClass('hide');

            if (bool) {
                this.$('.query-service-view-container').removeClass('hide');
            }

            this.$('.app-logo, .hp-logo-footer, .find-banner-container').toggleClass('hide', bool);

            this.removeDocumentDetailView();
            this.removeSuggestView();

            this.optionalViews.forEach(function (view) {
                view[bool ? 'onExpand' : 'onReduce'](view.instance);
            });

            // TODO: somebody else needs to own this
            $('.container-fluid, .find-logo-small').toggleClass('reduced', !bool);
            if (!this.configuration.hasBiRole) {
                // Hide the MOTD to make space for the search box
                $('.find-navbar-motd').toggleClass('fade', bool);
            }
        },

        isExpanded: function () {
            return this.$('.find').hasClass(expandedClasses);
        },

        removeDocumentDetailView: function () {
            if (this.documentDetailView) {
                this.documentDetailView.remove();
                this.stopListening(this.documentDetailView);
                this.documentDetailView = null;
            }
        },

        removeSuggestView: function () {
            if (this.suggestView) {
                this.suggestView.remove();
                this.stopListening(this.suggestView);
                this.suggestView = null;
            }
        },

        remove: function () {
            _.chain(this.optionalViews).pluck('instance').invoke('remove');

            this.savedQueryResultPoller.destroy();
            this.removeDocumentDetailView();
            Backbone.View.prototype.remove.call(this);
        },

        updateRouting: function (savedSearchModel, selectedTab) {
            let type = savedSearchModel.get('type');

            if (type === SavedSearchModel.Type.READ_ONLY_QUERY) { type = SavedSearchModel.Type.QUERY; }
            else if (type === SavedSearchModel.Type.READ_ONLY_SNAPSHOT) { type = SavedSearchModel.Type.SNAPSHOT; }

            const id = savedSearchModel.get('id');
            const modelId = type + ':' + id;

            vent.navigate(
                savedSearchModel.isNew() ? '/search/query' : '/search/tab/' + modelId + (selectedTab ? '/view/' + selectedTab : ''),
                {trigger: false}
            );

            this.currentRoute = Backbone.history.getFragment();
        },

        getSelectedRoute: function () {
            return this.currentRoute;
        },

        setLastNavigationOpts: function(queryText, databases) {
            this.lastNavigatedQueryText = queryText || false;
            this.lastNavigatedDatabases = databases || undefined;
        }
    });
});
