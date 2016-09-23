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
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/abstract-query-left-side-view.html'
], function(Backbone, _, FilterView, ConceptView, AppliedFiltersCollection, AppliedFiltersView, i18n, template) {
    'use strict';

    /**
     * View for displaying the filters currently applied to the search.
     *
     * Expected constructor arguments: queryState, indexesCollection
     */
    return Backbone.View.extend({
        // Abstract
        IndexesView: null,

        template: _.template(template),

        initialize: function(options) {
            this.conceptView = new ConceptView({
                queryState: options.queryState
            });

            this.filterView = new FilterView(_.extend({
                IndexesView: this.IndexesView
            }, options));

            this.appliedFiltersCollection = new AppliedFiltersCollection([], {
                queryState: options.queryState,
                indexesCollection: options.indexesCollection
            });

            this.appliedFiltersView = new AppliedFiltersView({collection: this.appliedFiltersCollection});
        },

        render: function() {
            this.sections = [
                {
                    view: this.conceptView,
                    title: i18n['search.concepts'],
                    containerClass: 'left-side-concepts-view'
                },
                {
                    view: this.appliedFiltersView,
                    title: i18n['search.filters.applied'],
                    containerClass: 'left-side-applied-filters-view'
                },
                {
                    view: this.filterView,
                    title: i18n['search.filters'],
                    containerClass: 'left-side-filters-view'
                }
            ];

            this.$el.html(this.template({sections: this.sections}));

            _.each(this.sections, function(section) {
                section.view
                    .setElement(this.$('.' + section.containerClass))
                    .render();
            }, this);
        },

        remove: function() {
            this.appliedFiltersCollection.stopListening();

            _.chain(this.sections)
                .pluck('view')
                .invoke('remove');

            Backbone.View.prototype.remove.call(this);
        }
    });
});
