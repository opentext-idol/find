/*
 * Copyright 2014-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'js-whatever/js/base-page',
    'find/app/configuration',
    'find/app/model/dates-filter-model',
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
    'find/app/page/search/document/document-detail-view',
    'find/app/page/search/results/query-strategy',
    'find/app/page/search/related-concepts/related-concepts-click-handlers',
    'find/app/util/database-name-resolver',
    'find/app/util/saved-query-result-poller',
    'find/app/util/events',
    'find/app/router',
    'find/app/vent',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/find-search.html'
], function(_, $, Backbone, BasePage, config, DatesFilterModel, SelectedParametricValuesCollection, DocumentsCollection,
            InputView, queryTextStrategy, TabbedSearchView, MergeCollection, SavedSearchModel,
            QueryMiddleColumnHeaderView, MinScoreModel, QueryTextModel, DocumentModel, DocumentDetailView,
            queryStrategy, relatedConceptsClickHandlers, databaseNameResolver, SavedQueryResultPoller, events,
            router, vent, i18n, template) {
    'use strict';

    var reducedClasses = 'reverse-animated-container col-md-offset-1 ' +
        'col-lg-offset-2 col-xs-12 col-sm-12 col-md-10 col-lg-8';
    var expandedClasses = 'animated-container col-sm-offset-0 col-md-offset-3 ' +
        'col-lg-offset-3 col-md-6 col-lg-6 col-xs-9 col-sm-9';

    var html = _.template(template)({i18n: i18n});

    function selectInitialIndexes(indexesCollection) {
        var privateIndexes = indexesCollection.reject({domain: 'PUBLIC_INDEXES'});
        var selectedIndexes;

        if(privateIndexes.length > 0) {
            selectedIndexes = privateIndexes;
        } else {
            selectedIndexes = indexesCollection.models;
        }

        return _.map(selectedIndexes, function(indexModel) {
            return indexModel.pick('domain', 'name');
        });
    }

    function fetchDocument(options, callback) {
        var documentModel = new DocumentModel();

        documentModel.fetch({
            data: {
                reference: options.reference,
                database: options.database
            }
        }).done(function() {
            callback(documentModel);
        });
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
                            this.expandedState();

                            // Create a tab if the user has run a search but has no open tabs
                            if(this.selectedTabModel.get('selectedSearchCid') === null) {
                                this.createNewTab(this.searchModel.get('inputText'));
                            }
                        }
                    });

                    return new InputView({
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
            this.indexesCollection = options.indexesCollection;
            this.windowScrollModel = options.windowScrollModel;

            this.searchTypes = this.getSearchTypes();

            this.searchCollections = _.mapObject(this.searchTypes, function(data) {
                return options[data.collection];
            });

            this.savedSearchCollection = new MergeCollection([], {
                collections: _.values(this.searchCollections)
            });

            this.selectedTabModel = new Backbone.Model({
                selectedSearchCid: null
            });

            // Model mapping saved search cids to query state
            this.queryStates = new Backbone.Model();

            // Map of saved search cid to ServiceView
            this.serviceViews = {};

            this.listenTo(this.selectedTabModel, 'change', this.selectContentView);

            this.listenTo(this.savedSearchCollection, 'remove', function(savedSearch) {
                var cid = savedSearch.cid;
                this.serviceViews[cid].view.remove();
                this.queryStates.unset(cid);
                delete this.serviceViews[cid];

                events(cid).abandon();

                if(this.selectedTabModel.get('selectedSearchCid') === cid) {
                    var lastModel = this.savedQueryCollection.last();

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
                    model: this.selectedTabModel,
                    queryStates: this.queryStates,
                    searchTypes: this.searchTypes
                });

                this.listenTo(this.tabView, 'startNewSearch', this.createNewTab);

                var savedSearchConfig = config().savedSearchConfig;
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

                this.reducedState();
            }, this);

            // Bind routing to search model
            this.listenTo(router, 'route:search', function(text) {
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

            this.listenTo(router, 'route:savedSearch', function(tab, resultsView) {

                if(this.savedSearchCollection.get(tab)) {
                    this.selectedTabModel.set({
                        'selectedSearchCid': this.savedSearchCollection.get(tab).cid,
                        'selectedResultsView': resultsView || ''
                    });
                }
                else {
                    this.listenToOnce(options.savedQueryCollection, 'update', function() {
                        if(this.savedSearchCollection.get(tab)) {
                            this.selectedTabModel.set({
                                'selectedSearchCid': this.savedSearchCollection.get(tab).cid,
                                'selectedResultsView': resultsView || ''
                            });
                        }
                    });
                }
            }, this);

            this.listenTo(router, 'route:documentDetail', function() {
                var backURL = this.suggestView
                    ? this.generateSuggestURL(this.suggestView.documentModel)
                    : this.generateURL();
                this.expandedState();
                this.$('.service-view-container').addClass('hide');
                this.$('.document-detail-service-view-container').removeClass('hide');

                this.removeDocumentDetailView();

                var options = this.documentDetailOptions.apply(this, arguments);

                fetchDocument(options, function(documentModel) {
                    this.documentDetailView = new DocumentDetailView({
                        backUrl: backURL,
                        model: documentModel,
                        indexesCollection: this.indexesCollection,
                        mmapTab: this.mmapTab
                    });

                    this.$('.document-detail-service-view-container').append(this.documentDetailView.$el);
                    this.documentDetailView.render();
                }.bind(this));
            }, this);

            this.listenTo(router, 'route:suggest', function() {
                this.expandedState();
                this.$('.service-view-container').addClass('hide');
                this.$('.suggest-service-view-container').removeClass('hide');

                var options = this.suggestOptions.apply(this, arguments);

                fetchDocument(options, function(documentModel) {
                    this.suggestView = new this.SuggestView({
                        backUrl: this.generateURL(),
                        documentModel: documentModel,
                        indexesCollection: this.indexesCollection,
                        scrollModel: this.windowScrollModel,
                        configuration: config(),
                        mmapTab: this.mmapTab
                    });

                    this.$('.suggest-service-view-container').append(this.suggestView.$el);
                    this.suggestView.render();
                }.bind(this));
            }, this);
        },

        render: function() {
            this.$el.html(html);

            this.optionalViews.forEach(function(view) {
                view.instance.setElement(this.$(view.selector)).render();
            }, this);

            if(this.tabView) {
                this.tabView.setElement(this.$('.search-tabs-container')).render();
            }

            if(this.selectedTabModel.get('selectedSearchCid') === null && !config().hasBiRole) {
                this.reducedState();
            } else {
                this.expandedState();
            }

            _.each(this.serviceViews, function(data) {
                this.$('.query-service-view-container').append(data.view.$el);
                data.view.render();
            }, this);

            if(config().hasBiRole && this.selectedTabModel.get('selectedSearchCid') === null) {
                this.createNewTab();
            } else {
                this.selectContentView();
            }
        },

        // Overrides method in BasePage
        update: function() {
            const viewData = this.serviceViews[this.selectedTabModel.get('selectedSearchCid')];

            if(viewData && viewData.view.update) {
                // Inform the service view that it is visible again so (e.g.) the topic map can be re-drawn
                viewData.view.update();
            }
        },

        // Can be overridden
        getSearchTypes: function() {
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
                    createSearchModelAttributes: function(conceptGroups) {
                        return {
                            inputString: conceptGroups.length > 0 ? conceptGroups.first().get('concepts')[0] : '*'
                        };
                    },
                    searchModelChange: function(options) {
                        return function() {
                            const inputText = options.searchModel.get('inputText');
                            if(inputText && inputText !== '*') {
                                options.queryState.conceptGroups.set([{concepts: [inputText], hidden: true}]);
                            } else {
                                options.queryState.conceptGroups.reset();
                            }
                        };
                    }
                }
            };
        },

        createNewTab: function(queryText) {
            var newSearch = new SavedSearchModel({
                relatedConcepts: queryText ? [[queryText]] : [],
                title: i18n['search.newSearch'],
                type: SavedSearchModel.Type.QUERY,
                minScore: config().minScore
            });

            this.savedQueryCollection.add(newSearch);
            this.selectedTabModel.set('selectedSearchCid', newSearch.cid);
        },

        selectContentView: function() {
            var cid = this.selectedTabModel.get('selectedSearchCid');

            _.each(this.serviceViews, function(data) {
                data.view.$el.addClass('hide');
            }, this);

            if(this.searchModel && this.searchChangeCallback !== null) {
                this.stopListening(this.searchModel, 'change', this.searchChangeCallback);
                this.searchChangeCallback = null;
            }

            if(cid) {
                let viewData;
                const savedSearchModel = this.savedSearchCollection.get(cid);
                const searchType = savedSearchModel.get('type');

                events(cid);

                const modelId = this.savedSearchCollection.modelId(savedSearchModel.attributes);
                if(this.serviceViews[cid]) {
                    viewData = this.serviceViews[cid];
                } else {
                    const minScore = new MinScoreModel({minScore: 0});
                    const documentsCollection = new this.searchTypes[searchType].DocumentsCollection();

                    let initialSelectedIndexes;
                    const savedSelectedIndexes = savedSearchModel.toSelectedIndexes();

                    if(savedSelectedIndexes.length === 0) {
                        if(this.indexesCollection.isEmpty()) {
                            initialSelectedIndexes = [];
                        } else {
                            initialSelectedIndexes = selectInitialIndexes(this.indexesCollection);
                        }
                    } else {
                        initialSelectedIndexes = savedSelectedIndexes;
                    }

                    /**
                     * @type {QueryState}
                     */
                    const queryState = {
                        conceptGroups: new Backbone.Collection(savedSearchModel.toConceptGroups()),
                        minScoreModel: minScore,
                        datesFilterModel: new DatesFilterModel(savedSearchModel.toDatesFilterModelAttributes()),
                        selectedIndexes: new this.IndexesCollection(initialSelectedIndexes),
                        selectedParametricValues: new SelectedParametricValuesCollection(savedSearchModel.toSelectedParametricValues())
                    };

                    this.queryStates.set(cid, queryState);

                    viewData = {
                        queryState: queryState,
                        documentsCollection: documentsCollection,
                        view: new this.ServiceView(_.extend({
                            delayedIndexesSelection: selectInitialIndexes,
                            documentsCollection: documentsCollection,
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

                    this.listenTo(viewData.view, 'updateRouting', _.bind(this.updateRouting, this, modelId));
                }

                if(this.searchModel) {
                    this.searchModel.set(this.searchTypes[searchType].createSearchModelAttributes(viewData.queryState.conceptGroups));

                    const changeListenerOptions = {
                        savedQueryCollection: this.savedQueryCollection,
                        selectedTabModel: this.selectedTabModel,
                        searchModel: this.searchModel,
                        queryState: viewData.queryState
                    };

                    this.searchChangeCallback = this.searchTypes[searchType].searchModelChange(changeListenerOptions);
                    this.listenTo(this.searchModel, 'change', this.searchChangeCallback);
                }

                viewData.view.$el.removeClass('hide');

                if(viewData.view.update) {
                    viewData.view.update();
                }

                if(this.selectedTabModel.get('selectedResultsView')) {
                    viewData.view.changeTab(this.selectedTabModel.get('selectedResultsView'));
                    this.selectedTabModel.set('selectedResultsView', '');
                }

                this.updateRouting(modelId, viewData.view.getSelectedTab());
            }
        },

        generateURL: function() {
            if(this.searchModel && this.searchModel.get('inputText')) {
                var inputText = this.searchModel.get('inputText');
                return 'search/query/' + encodeURIComponent(inputText);
            } else {
                if(this.selectedTabModel.get('selectedSearchCid') || config().hasBiRole) {
                    return 'search/query';
                } else {
                    return 'search/splash';
                }
            }
        },

        generateSuggestURL: function(model) {
            return 'search/suggest/' + vent.addSuffixForDocument(model);
        },

        // Run fancy animation from large central search bar to main search page
        expandedState: function() {
            this.$('.find').removeClass(reducedClasses).addClass(expandedClasses);

            this.$('.service-view-container').addClass('hide');
            this.$('.query-service-view-container').removeClass('hide');
            this.$('.app-logo').addClass('hide');
            this.$('.hp-logo-footer').addClass('hide');

            this.removeDocumentDetailView();
            this.removeSuggestView();

            this.optionalViews.forEach(function(view) {
                view.onExpand(view.instance)
            });
            this.$('.find-banner-container').addClass('hide');

            // TODO: somebody else needs to own this
            $('.container-fluid, .find-logo-small').removeClass('reduced');
        },

        // Set view to initial state (large central search bar)
        reducedState: function() {
            this.$('.find').removeClass(expandedClasses).addClass(reducedClasses);

            this.$('.service-view-container').addClass('hide');
            this.$('.app-logo').removeClass('hide');
            this.$('.hp-logo-footer').removeClass('hide');

            this.removeDocumentDetailView();
            this.removeSuggestView();

            this.optionalViews.forEach(function(view) {
                view.onReduce(view.instance)
            });
            this.$('.find-banner-container').removeClass('hide');

            // TODO: somebody else needs to own this
            $('.container-fluid, .find-logo-small').addClass('reduced');
        },

        isExpanded: function() {
            return this.$('.find').hasClass(expandedClasses);
        },

        removeDocumentDetailView: function() {
            if(this.documentDetailView) {
                this.documentDetailView.remove();
                this.stopListening(this.documentDetailView);
                this.documentDetailView = null;
            }
        },

        removeSuggestView: function() {
            if(this.suggestView) {
                this.suggestView.remove();
                this.stopListening(this.suggestView);
                this.suggestView = null;
            }
        },

        remove: function() {
            _.chain(this.optionalViews).pluck('instance').invoke('remove');

            this.savedQueryResultPoller.destroy();
            this.removeDocumentDetailView();
            Backbone.View.prototype.remove.call(this);
        },

        updateRouting: function(savedSearch, selectedTab) {
            if(savedSearch) {
                vent.navigate('/search/tab/' + savedSearch + (selectedTab ? '/view/' + selectedTab : ''), {trigger: false});
            } else {
                vent.navigate('/search/query', {trigger: false});
            }

            this.currentRoute = Backbone.history.getFragment();
        },

        getSelectedRoute: function() {
            return this.currentRoute;
        }
    });
});
