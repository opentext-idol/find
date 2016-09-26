/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
define([
    'backbone',
    'underscore',
    './filter-view',
    './selected-concepts/concept-view',
    'find/app/model/search-filters-collection',
    'find/app/page/search/filter-display/filter-display-view',
    'i18n!find/nls/bundle'
], function(Backbone, _, FilterView, ConceptView, AppliedFiltersCollection, FilterDisplayView, i18n) {
    'use strict';

    /**
     * View for displaying the filters currently applied to the search.
     *
     * Expected constructor arguments: queryState, indexesCollection
     */
    return Backbone.View.extend({
        // Abstract
        IndexesView: null,

        initialize: function(options) {
            this.appliedFiltersCollection = new AppliedFiltersCollection([], {
                indexesCollection: options.indexesCollection,
                queryState: options.queryState
            });

            this.views = [
                new ConceptView({
                    queryState: options.queryState,
                    title: i18n['search.concepts']
                }),
                new FilterDisplayView({
                    collection: this.appliedFiltersCollection,
                    title: i18n['search.filters.applied'],
                    titleClass: 'inline-block'
                }),
                new FilterView(_.extend({
                    IndexesView: this.IndexesView,
                    title: i18n['search.filters']
                }, options))
            ]
        },

        render: function() {
            _.each(this.views, function(view) {
                this.$el.append(view.$el);

                view.render();
            }, this);
        },

        remove: function() {
            this.appliedFiltersCollection.stopListening();

            _.chain(this.views)
                .invoke('remove');

            Backbone.View.prototype.remove.call(this);
        }
    });
});
