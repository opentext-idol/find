/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone',
    './filter-view',
    './selected-concepts/concept-view',
    'find/app/model/applied-filters-collection',
    'find/app/page/search/filter-display/applied-filters-view',
    'i18n!find/nls/bundle'
], function(_, Backbone, FilterView, ConceptView, AppliedFiltersCollection, AppliedFiltersView,
            i18n) {
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
                queryState: options.queryState,
                indexesCollection: options.indexesCollection
            });

            this.sections = [
                new ConceptView({
                    configuration: options.configuration,
                    queryState: options.queryState,
                    title: i18n['search.concepts'],
                    containerClass: 'left-side-concepts-view'
                }),
                new AppliedFiltersView({
                    collection: this.appliedFiltersCollection,
                    title: i18n['search.filters.applied'],
                    containerClass: 'left-side-applied-filters-view',
                    titleClass: 'block'
                }),
                new FilterView(_.extend({
                    IndexesView: this.IndexesView,
                    title: i18n['search.filters'],
                    containerClass: 'left-side-filters-view'
                }, options))
            ];
        },

        render: function() {
            _.each(this.sections, function(section) {
                this.$el.append(section.$el);
                section.render();
            }, this);
        },

        remove: function() {
            this.appliedFiltersCollection.stopListening();

            _.chain(this.sections)
                .invoke('remove');

            Backbone.View.prototype.remove.call(this);
        }
    });
});
