/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
                    titleClass: ''
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
