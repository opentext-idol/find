/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/base-page',
    'backbone',
    'find/app/model/search-page-model',
    'find/app/model/dates-filter-model',
    'parametric-refinement/selected-values-collection',
    'find/app/model/indexes-collection',
    'find/app/page/search/input-view',
    'find/app/page/search/tabbed-search-view',
    'find/app/model/saved-searches/saved-search-collection',
    'find/app/util/model-any-changed-attribute-listener',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/model/query-text-model',
    'find/app/router',
    'find/app/vent',
    'i18n!find/nls/bundle',
    'underscore',
    'text!find/templates/app/page/find-search.html'
], function(BasePage, Backbone, SearchPageModel, DatesFilterModel, SelectedParametricValuesCollection, IndexesCollection, InputView, TabbedSearchView, SavedSearchCollection,
            addChangeListener, SavedSearchModel, QueryTextModel, router, vent, i18n, _, template) {

    'use strict';

    var reducedClasses = 'reverse-animated-container col-sm-offset-1 col-md-offset-2 col-lg-offset-3 col-xs-12 col-sm-10 col-md-8 col-lg-6';
    var expandedClasses = 'animated-container col-sm-offset-1 col-md-offset-2 col-xs-12 col-sm-10 col-md-7';
    var QUERY_TEXT_MODEL_ATTRIBUTES = ['inputText', 'relatedConcepts'];

    function selectInitialIndexes(indexesCollection) {
        var privateIndexes = indexesCollection.reject({domain: 'PUBLIC_INDEXES'});
        var selectedIndexes;

        if (privateIndexes.length > 0) {
            selectedIndexes = privateIndexes;
        } else {
            selectedIndexes = indexesCollection.models;
        }

        return _.map(selectedIndexes, function(indexModel) {
            return indexModel.pick('domain', 'name');
        });
    }

    return BasePage.extend({
        className: 'search-page',
        template: _.template(template),

        // Abstract
        ServiceView: null,

        initialize: function() {
            this.savedSearchCollection = new SavedSearchCollection();
            this.savedSearchCollection.fetch({remove: false});

            this.indexesCollection = new IndexesCollection();
            this.indexesCollection.fetch();

            // Model representing high level search page state
            this.searchModel = new SearchPageModel();

            // Model mapping saved search cids to query state
            this.queryStates = new Backbone.Model();

            // Map of saved search cid to ServiceView
            this.serviceViews = {};

            this.listenTo(this.searchModel, 'change:selectedSearchCid', this.selectContentView);

            this.listenTo(this.searchModel, 'change', function() {
                // Bind search model to routing
                vent.navigate(this.generateURL(), {trigger: false});

                if (this.searchModel.get('inputText')) {
                    this.expandedState();
                }

                // Create a tab if the user has run a search but has no open tabs
                if (this.searchModel.get('selectedSearchCid') === null && this.searchModel.get('inputText')) {
                    this.createNewTab();
                }
            });

            addChangeListener(this, this.searchModel, QUERY_TEXT_MODEL_ATTRIBUTES, function() {
                var selectedSearchCid = this.searchModel.get('selectedSearchCid');

                if (selectedSearchCid) {
                    var queryTextModel = this.serviceViews[selectedSearchCid].queryTextModel;
                    queryTextModel.set(this.searchModel.pick('inputText', 'relatedConcepts'));
                }
            });

            this.listenTo(this.savedSearchCollection, 'remove', function(savedSearch) {
                var cid = savedSearch.cid;
                this.serviceViews[cid].view.remove();
                this.queryStates.unset(cid);
                delete this.serviceViews[cid];

                if (this.searchModel.get('selectedSearchCid') === cid) {
                    var lastModel = this.savedSearchCollection.last();

                    if (lastModel) {
                        this.searchModel.set('selectedSearchCid', lastModel.cid);
                    } else {
                        // If the user closes their last tab, run a search for *
                        this.searchModel.set({
                            selectedSearchCid: null,
                            inputText: '*',
                            relatedConcepts: []
                        });
                    }
                }
            });

            this.inputView = new InputView({model: this.searchModel});

            this.tabView = new TabbedSearchView({
                savedSearchCollection: this.savedSearchCollection,
                searchModel: this.searchModel,
                queryStates: this.queryStates
            });

            this.listenTo(this.tabView, 'startNewSearch', this.createNewTab);

            // Bind routing to search model
            router.on('route:search', function(text, concepts) {
                // The concepts string starts with a leading /
                var conceptsArray = concepts ? _.tail(concepts.split('/')) : [];

                this.searchModel.set({
                    inputText: text || '',
                    relatedConcepts: conceptsArray
                });
            }, this);
        },

        render: function() {
            this.$el.html(this.template);

            this.inputView.setElement(this.$('.input-view-container')).render();
            this.tabView.setElement(this.$('.tabbed-search-row')).render();

            if (this.searchModel.get('selectedSearchCid') === null) {
                this.reducedState();
            } else {
                this.expandedState();
            }
            _.each(this.serviceViews, function(data) {
                this.$('.top-options-container').after(data.view.$el);
                data.view.render();
            }, this);

            this.selectContentView();
        },

        createNewTab: function() {
            var newSearch = new SavedSearchModel({
                queryText: this.searchModel.get('inputText'),
                relatedConcepts: this.searchModel.get('relatedConcepts'),
                title: i18n['search.newSearch']
            });

            this.savedSearchCollection.add(newSearch);
            this.searchModel.set('selectedSearchCid', newSearch.cid);
        },

        selectContentView: function() {
            var cid = this.searchModel.get('selectedSearchCid');

            _.each(this.serviceViews, function(data) {
                data.view.$el.addClass('hide');
                this.stopListening(data.queryTextModel);
            }, this);

            if (cid) {
                var viewData;
                var savedSearchModel = this.savedSearchCollection.get(cid);

                if (this.serviceViews[cid]) {
                    viewData = this.serviceViews[cid];
                } else {
                    var queryTextModel = new QueryTextModel(savedSearchModel.toQueryTextModelAttributes());

                    var queryState = {
                        queryTextModel: queryTextModel,
                        datesFilterModel: new DatesFilterModel(savedSearchModel.toDatesFilterModelAttributes()),
                        selectedParametricValues: new SelectedParametricValuesCollection(savedSearchModel.toSelectedParametricValues())
                    };

                    var initialSelectedIndexes;
                    var savedSelectedIndexes = savedSearchModel.toSelectedIndexes();

                    // TODO: Check if the saved indexes still exists?
                    if (savedSelectedIndexes.length === 0) {
                        if (this.indexesCollection.isEmpty()) {
                            initialSelectedIndexes = [];

                            this.listenToOnce(this.indexesCollection, 'sync', function() {
                                queryState.selectedIndexes.set(selectInitialIndexes(this.indexesCollection));
                            });
                        } else {
                            initialSelectedIndexes = selectInitialIndexes(this.indexesCollection);
                        }
                    } else {
                        initialSelectedIndexes = savedSelectedIndexes;
                    }

                    queryState.selectedIndexes = new IndexesCollection(initialSelectedIndexes);

                    this.queryStates.set(cid, queryState);

                    this.serviceViews[cid] = viewData = {
                        queryTextModel: queryTextModel,
                        view: new this.ServiceView({
                            indexesCollection: this.indexesCollection,
                            searchModel: this.searchModel,
                            savedSearchCollection: this.savedSearchCollection,
                            queryState: queryState,
                            savedSearchModel: savedSearchModel
                        })
                    };

                    this.$('.top-options-container').after(viewData.view.$el);
                    viewData.view.render();
                }

                addChangeListener(this, viewData.queryTextModel, QUERY_TEXT_MODEL_ATTRIBUTES, function() {
                    this.searchModel.set(viewData.queryTextModel.pick(QUERY_TEXT_MODEL_ATTRIBUTES));
                });

                this.searchModel.set(viewData.queryTextModel.pick(QUERY_TEXT_MODEL_ATTRIBUTES));
                viewData.view.$el.removeClass('hide');
            }
        },

        generateURL: function() {
            var components = [this.searchModel.get('inputText')].concat(this.searchModel.get('relatedConcepts'));
            return 'find/search/' + _.map(components, encodeURIComponent).join('/');
        },

        // Run fancy animation from large central search bar to main search page
        expandedState: function() {
            this.$('.find').removeClass(reducedClasses).addClass(expandedClasses);

            this.$('.tabbed-search-row').show();
            this.$('.app-logo').hide();
            this.$('.hp-logo-footer').addClass('hidden');

            // TODO: somebody else needs to own this
            $('.find-banner-container').removeClass('reduced navbar navbar-static-top').find('>').show();
            $('.container-fluid, .find-logo-small').removeClass('reduced');
        },

        // Set view to initial state (large central search bar)
        reducedState: function() {
            this.$('.find').removeClass(expandedClasses).addClass(reducedClasses);

            this.$('.tabbed-search-row').hide();
            this.$('.app-logo').show();
            this.$('.hp-logo-footer').removeClass('hidden');

            // TODO: somebody else needs to own this
            $('.find-banner-container').addClass('reduced navbar navbar-static-top').find('>').hide();
            $('.container-fluid, .find-logo-small').addClass('reduced');
        }
    });
});
