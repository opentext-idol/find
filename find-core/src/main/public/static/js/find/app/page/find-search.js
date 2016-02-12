/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/base-page',
    'backbone',
    'find/app/model/search-page-model',
    'find/app/model/indexes-collection',
    'find/app/model/documents-collection',
    'find/app/page/search/input-view',
    'find/app/page/search/tabbed-search-view',
    'find/app/model/saved-searches/saved-search-collection',
    'find/app/util/model-any-changed-attribute-listener',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/model/query-text-model',
    'find/app/model/document-model',
    'find/app/page/search/document/document-detail-view',
    'find/app/util/database-name-resolver',
    'find/app/router',
    'find/app/vent',
    'i18n!find/nls/bundle',
    'underscore',
    'text!find/templates/app/page/find-search.html'
], function(BasePage, Backbone, SearchPageModel, IndexesCollection, DocumentsCollection, InputView, TabbedSearchView, SavedSearchCollection,
            addChangeListener, SavedSearchModel, QueryTextModel, DocumentModel, DocumentDetailView, databaseNameResolver, router, vent, i18n, _, template) {

    'use strict';

    var reducedClasses = 'reverse-animated-container col-sm-offset-1 col-md-offset-2 col-lg-offset-3 col-xs-12 col-sm-10 col-md-8 col-lg-6';
    var expandedClasses = 'animated-container col-sm-offset-1 col-md-offset-2 col-xs-12 col-sm-10 col-md-7';
    var QUERY_TEXT_MODEL_ATTRIBUTES = ['inputText', 'relatedConcepts'];

    return BasePage.extend({
        className: 'search-page',
        template: _.template(template),

        // Abstract
        ServiceView: null,
        documentDetailOptions: null,

        initialize: function() {
            this.savedSearchCollection = new SavedSearchCollection();
            this.savedSearchCollection.fetch({remove: false});

            this.indexesCollection = new IndexesCollection();
            this.indexesCollection.fetch();

            // Model representing high level search page state
            this.searchModel = new SearchPageModel();

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
                    var newSearch = new SavedSearchModel({
                        queryText: this.searchModel.get('inputText'),
                        relatedConcepts: this.searchModel.get('relatedConcepts'),
                        title: i18n['search.newSearch']
                    });

                    this.savedSearchCollection.add(newSearch);
                    this.searchModel.set('selectedSearchCid', newSearch.cid);
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
                searchModel: this.searchModel
            });

            router.on('route:emptySearch', this.reducedState, this);

            // Bind routing to search model
            router.on('route:search', function(text, concepts) {
                this.removeDocumentDetailView();

                // The concepts string starts with a leading /
                var conceptsArray = concepts ? _.tail(concepts.split('/')) : [];

                this.searchModel.set({
                    inputText: text || '',
                    relatedConcepts: conceptsArray
                });

                this.$('.service-view-container').addClass('hide');
                this.$('.query-service-view-container').removeClass('hide');
            }, this);

            router.on('route:documentDetail', function () {
                this.expandedState();
                this.$('.service-view-container').addClass('hide');
                this.$('.document-detail-service-view-container').removeClass('hide');

                var options = this.documentDetailOptions.apply(this, arguments);
                this.populateDocumentModelForDetailView(options);
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
                this.$('.query-service-view-container').append(data.view.$el);
                data.view.render();
            }, this);

            this.selectContentView();
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
                    var documentsCollection = new DocumentsCollection();

                    this.serviceViews[cid] = viewData = {
                        queryTextModel: queryTextModel,
                        documentsCollection: documentsCollection,
                        view: new this.ServiceView({
                            indexesCollection: this.indexesCollection,
                            documentsCollection: documentsCollection,
                            searchModel: this.searchModel,
                            savedSearchCollection: this.savedSearchCollection,
                            queryTextModel: queryTextModel,
                            savedSearchModel: savedSearchModel
                        })
                    };

                    this.$('.query-service-view-container').append(viewData.view.$el);
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

            if(_.compact(components).length) {
                return 'find/search/query/' + _.map(components, encodeURIComponent).join('/');
            } else {
                return 'find/search/query'
            }
        },

        // Run fancy animation from large central search bar to main search page
        expandedState: function() {
            this.$('.find').removeClass(reducedClasses).addClass(expandedClasses);

            this.$('.query-service-view-container').removeClass('hide');
            this.$('.app-logo').addClass('hide');
            this.$('.hp-logo-footer').addClass('hide');

            // TODO: somebody else needs to own this
            $('.find-banner-container').removeClass('reduced navbar navbar-static-top').find('>').removeClass('hide');
            $('.container-fluid, .find-logo-small').removeClass('reduced');
        },

        // Set view to initial state (large central search bar)
        reducedState: function() {
            this.$('.find').removeClass(expandedClasses).addClass(reducedClasses);

            this.$('.service-view-container').addClass('hide');
            this.$('.app-logo').removeClass('hide');
            this.$('.hp-logo-footer').removeClass('hide');

            this.removeDocumentDetailView();

            // TODO: somebody else needs to own this
            $('.find-banner-container').addClass('reduced navbar navbar-static-top').find('>').addClass('hide');
            $('.container-fluid, .find-logo-small').addClass('reduced');
        },

        // If we already have the document model in one of our collections, then don't bother fetching it
        populateDocumentModelForDetailView: function(options) {
            var model;
            _.find(this.serviceViews, function(serviceView) {
                var document = serviceView.documentsCollection.find(function(collectionModel) {
                    return collectionModel.get('reference') === options.reference &&
                        databaseNameResolver.resolveDatabaseNameForDocumentModel(collectionModel) === options.database;
                });

                if(document) {
                    // stop looping once we find the model
                    model = document;
                    return true;
                } else {
                    return false;
                }
            });

            if (model) {
                this.renderDocumentDetail(model)
            } else {
                (new DocumentModel()).fetch({
                    data: {
                        reference: options.reference,
                        database: options.database
                    },
                    success: _.bind(this.renderDocumentDetail, this)
                });
            }
        },

        renderDocumentDetail: function(model) {
            this.documentDetailView = new DocumentDetailView({
                backUrl: this.generateURL(),
                model: model
            });

            this.$('.document-detail-service-view-container').append(this.documentDetailView.$el);
            this.documentDetailView.render();
        },

        removeDocumentDetailView: function() {
            if (this.documentDetailView) {
                this.documentDetailView.remove();
                this.stopListening(this.documentDetailView);
                this.documentDetailView = null;
            }
        }
    });
});
