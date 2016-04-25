/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/page/search/filters/date/dates-filter-view',
    'find/app/page/search/filters/parametric/parametric-view',
    'find/app/util/text-input',
    'find/app/util/collapsible',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes'
], function(Backbone, DateView, ParametricView, TextInput, Collapsible, i18n, i18nIndexes) {

    return Backbone.View.extend({
        // Abstract
        IndexesView: null,

        initialize: function(options) {
            var filterModel = new Backbone.Model();

            this.filterInput = new TextInput({
                model: filterModel,
                modelAttribute: 'text',
                templateOptions: {
                    placeholder: i18n['search.filters.filter']
                }
            });

            var indexesView = new this.IndexesView({
                queryModel: options.queryModel,
                indexesCollection: options.indexesCollection,
                selectedDatabasesCollection: options.queryState.selectedIndexes,
                filterModel: filterModel
            });

            var dateView = new DateView({
                datesFilterModel: options.queryState.datesFilterModel,
                savedSearchModel: options.savedSearchModel
            });

            this.parametricView = new ParametricView({
                queryModel: options.queryModel,
                queryState: options.queryState,
                filterModel: filterModel,
                indexesCollection: options.indexesCollection,
                parametricCollection: options.parametricCollection
            });

            this.indexesViewWrapper = new Collapsible({
                view: indexesView,
                collapsed: false,
                title: i18nIndexes['search.indexes']
            });

            this.dateViewWrapper = new Collapsible({
                view: dateView,
                collapsed: false,
                title: i18n['search.dates']
            });
        },

        render: function() {
            this.$el.empty()
                .append(this.filterInput.$el)
                .append(this.indexesViewWrapper.$el)
                .append(this.dateViewWrapper.$el)
                .append(this.parametricView.$el);

            this.filterInput.render();
            this.indexesViewWrapper.render();
            this.parametricView.render();
            this.dateViewWrapper.render();

            return this;
        },

        remove: function() {
            _.invoke([
                this.parametricView,
                this.indexesViewWrapper,
                this.dateViewWrapper
            ], 'remove');

            Backbone.View.prototype.remove.call(this);
        }
    });

});
