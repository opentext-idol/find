/*
 * Copyright 2016-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

const _ = require('underscore');
const Backbone = require('backbone');
const FilterView = require('./filter-view');
const ConceptView = require('./selected-concepts/concept-view');
const AppliedFiltersCollection = require('find/app/model/applied-filters-collection');
const AppliedFiltersView = require('find/app/page/search/filter-display/applied-filters-view');
const i18n = require('find/nls/bundle');

/**
 * View for displaying the filters currently applied to the search.
 *
 * Expected constructor arguments: queryState, indexesCollection
 */

module.exports = Backbone.View.extend({
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

