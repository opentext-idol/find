/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/base-page',
    'backbone',
    'find/app/model/search-page-model',
    'find/app/model/indexes-collection',
    'find/app/page/search/input-view',
    'find/app/page/search/tabbed-search-view',
    'find/app/model/saved-searches/saved-search-collection',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/router',
    'find/app/vent',
    'i18n!find/nls/bundle',
    'underscore',
    'text!find/templates/app/page/find-search.html'
], function(BasePage, Backbone, SearchPageModel, IndexesCollection, InputView, TabbedSearchView, SavedSearchCollection, SavedSearchModel, router, vent, i18n, _, template) {

    'use strict';

    var reducedClasses = 'reverse-animated-container col-sm-offset-1 col-md-offset-2 col-lg-offset-3 col-xs-12 col-sm-10 col-md-8 col-lg-6';
    var expandedClasses = 'animated-container col-sm-offset-1 col-md-offset-2 col-xs-12 col-sm-10 col-md-7';

    return BasePage.extend({
        className: 'search-page',
        template: _.template(template),

        // Abstract
        ServiceView: null,

        initialize: function() {
            this.savedSearchCollection = new SavedSearchCollection();
            this.savedSearchCollection.fetch({remove: false});

            var indexesCollection = new IndexesCollection();
            indexesCollection.fetch();

            // Model representing high level search page state
            this.searchModel = new SearchPageModel({
                inputText: '',
                selectedSearchCid: null
            });

            this.listenTo(this.searchModel, 'change', function() {
                var inputText = this.searchModel.get('inputText');

                // Bind search model to routing
                vent.navigate(this.generateURL(), {trigger: false});

                // Create a tab if the user has run a search but has no open tabs
                if (inputText && this.searchModel.get('selectedSearchCid') === null) {
                    var newSearch = new SavedSearchModel({
                        queryText: this.searchModel.makeQueryText(),
                        title: i18n['search.newSearch']
                    });

                    this.savedSearchCollection.add(newSearch);
                    this.searchModel.set('selectedSearchCid', newSearch.cid);
                    this.expandedState();
                }
            });

            this.inputView = new InputView({model: this.searchModel});

            this.tabView = new TabbedSearchView({
                indexesCollection: indexesCollection,
                savedSearchCollection: this.savedSearchCollection,
                searchModel: this.searchModel,
                ServiceView: this.ServiceView
            });

            // Bind routing to search model
            router.on('route:search', function(text, concepts) {
                var attributes = {
                    inputText: text || '',
                    relatedConcepts: concepts ? concepts.split('/') : []
                };

                this.searchModel.setInputText(attributes);
            }, this);
        },

        render: function() {
            this.$el.html(this.template);

            this.inputView.setElement(this.$('.input-view-container')).render();
            this.tabView.setElement(this.$('.tabbed-search-container')).render();

            if (this.searchModel.get('selectedSearchCid') === null) {
                this.reducedState();
            } else {
                this.expandedState();
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
