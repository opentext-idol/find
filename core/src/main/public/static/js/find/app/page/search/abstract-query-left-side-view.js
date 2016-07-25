/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/page/search/filters/date/dates-filter-view',
    'find/app/page/search/filters/parametric/parametric-view',
    'find/app/page/search/filters/parametric/numeric-parametric-view',
    'find/app/page/search/filters/parametric/numeric-parametric-field-view',
    'find/app/util/text-input',
    'find/app/util/collapsible',
    'find/app/util/filtering-collection',
    'parametric-refinement/prettify-field-name',
    'parametric-refinement/display-collection',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes'
], function(Backbone, $, _, DateView, ParametricView, NumericParametricView, NumericParametricFieldView,
            TextInput, Collapsible, FilteringCollection, prettifyFieldName, ParametricDisplayCollection, configuration, i18n, i18nIndexes) {
    "use strict";

    var datesTitle = i18n['search.dates'];

    function filterPredicate(filterModel, model) {
        var searchText = filterModel.get('text');
        return searchText ? searchMatches(prettifyFieldName(model.id), filterModel.get('text')) : true;
    }

    function searchMatches(text, search) {
        return text.toLowerCase().indexOf(search.toLowerCase()) > -1;
    }

    return Backbone.View.extend({
        // Abstract
        IndexesView: null,

        initialize: function(options) {
            this.filterModel = new Backbone.Model();
            this.timeBarModel = options.timeBarModel;

            this.filterInput = new TextInput({
                model: this.filterModel,
                modelAttribute: 'text',
                templateOptions: {
                    placeholder: i18n['search.filters.filter']
                }
            });

            this.indexesEmpty = false;

            //noinspection JSUnresolvedFunction
            var indexesView = new this.IndexesView({
                queryModel: options.queryModel,
                indexesCollection: options.indexesCollection,
                selectedDatabasesCollection: options.queryState.selectedIndexes,
                filterModel: this.filterModel,
                visibleIndexesCallback: _.bind(function(indexes) {
                    this.indexesEmpty = indexes.length === 0;
                    this.updateIndexesVisibility();
                    this.updateEmptyMessage();
                }, this)
            });

            var dateView = new DateView({
                datesFilterModel: options.queryState.datesFilterModel,
                savedSearchModel: options.savedSearchModel
            });

            this.numericParametricFieldsCollection = options.numericParametricFieldsCollection;
            this.dateParametricFieldsCollection = options.dateParametricFieldsCollection;

            var createFilteringCollection = function(baseCollection) {
                return new FilteringCollection([], {
                    collection: baseCollection,
                    filterModel: this.filterModel,
                    predicate: filterPredicate,
                    resetOnFilter: false
                });
            }.bind(this);

            this.filteredNumericCollection = createFilteringCollection(this.numericParametricFieldsCollection);
            this.filteredDateCollection = createFilteringCollection(this.dateParametricFieldsCollection);

            this.parametricDisplayCollection = new ParametricDisplayCollection([], {
                parametricCollection: options.restrictedParametricCollection,
                selectedParametricValues: options.queryState.selectedParametricValues,
                filterModel: this.filterModel
            });

            //noinspection JSUnresolvedFunction
            this.listenTo(this.parametricDisplayCollection, 'update reset', function() {
                this.updateParametricVisibility();
                this.updateEmptyMessage();
            });

            this.numericParametricView = new NumericParametricView({
                queryModel: options.queryModel,
                queryState: options.queryState,
                timeBarModel: options.timeBarModel,
                dataType: 'numeric',
                collection: this.filteredNumericCollection,
                numericRestriction: true
            });

            this.dateParametricView = new NumericParametricView({
                queryModel: options.queryModel,
                queryState: options.queryState,
                timeBarModel: options.timeBarModel,
                dataType: 'date',
                collection: this.filteredDateCollection,
                inputTemplate: NumericParametricFieldView.dateInputTemplate,
                formatting: NumericParametricFieldView.dateFormatting
            });

            this.parametricView = new ParametricView({
                queryModel: options.queryModel,
                queryState: options.queryState,
                filterModel: this.filterModel,
                indexesCollection: options.indexesCollection,
                parametricCollection: options.parametricCollection,
                restrictedParametricCollection: options.restrictedParametricCollection,
                displayCollection: this.parametricDisplayCollection
            });

            this.collapsed = {
                dates: false,
                indexes: false
            };

            this.indexesViewWrapper = new Collapsible({
                view: indexesView,
                collapsed: this.collapsed.indexes,
                title: i18nIndexes['search.indexes']
            });

            this.dateViewWrapper = new Collapsible({
                view: dateView,
                collapsed: this.collapsed.dates,
                title: datesTitle
            });

            //noinspection JSUnresolvedFunction
            this.listenTo(this.filterModel, 'change', function() {
                this.updateDatesVisibility();
                this.updateParametricVisibility();
                this.updateEmptyMessage();
            });

            this.$emptyMessage = $('<p class="hide">' + i18n['search.filters.empty'] + '</p>');

            // only track user triggered changes, not automatic ones
            this.listenTo(this.indexesViewWrapper, 'toggle', function(newState) {
                this.collapsed.indexes = newState;
            });

            this.listenTo(this.dateViewWrapper, 'toggle', function(newState) {
                this.collapsed.dates = newState;
            });
        },

        render: function() {
            //noinspection JSUnresolvedVariable
            this.$el.empty()
                .append(this.filterInput.$el)
                .append(this.$emptyMessage)
                .append(this.indexesViewWrapper.$el)
                .append(this.dateViewWrapper.$el)
                .append(this.numericParametricView.$el)
                .append(this.dateParametricView.$el)
                .append(this.parametricView.$el);

            this.filterInput.render();
            this.indexesViewWrapper.render();
            this.numericParametricView.render();
            this.dateParametricView.render();
            this.parametricView.render();
            this.dateViewWrapper.render();

            this.updateParametricVisibility();
            this.updateDatesVisibility();
            this.updateIndexesVisibility();
            this.updateEmptyMessage();

            return this;
        },

        remove: function() {
            //noinspection JSUnresolvedFunction
            _.invoke([
                this.numericParametricView,
                this.dateParametricView,
                this.parametricView,
                this.indexesViewWrapper,
                this.dateViewWrapper
            ], 'remove');

            Backbone.View.prototype.remove.call(this);
        },

        updateEmptyMessage: function() {
            var noFiltersMatched = !(this.indexesEmpty && this.hideDates && this.parametricDisplayCollection.length === 0 && this.filteredNumericCollection.length === 0 && this.filteredDateCollection.length === 0);

            this.$emptyMessage.toggleClass('hide', noFiltersMatched);
        },

        updateParametricVisibility: function() {
            this.numericParametricView.$el.toggleClass('hide', this.numericParametricFieldsCollection.length === 0 && Boolean(this.filterModel.get('text')));
            this.dateParametricView.$el.toggleClass('hide', this.dateParametricFieldsCollection.length === 0 && Boolean(this.filterModel.get('text')));
            this.parametricView.$el.toggleClass('hide', this.parametricDisplayCollection.length === 0 && Boolean(this.filterModel.get('text')));
        },

        updateDatesVisibility: function() {
            var search = this.filterModel.get('text');
            this.hideDates = !(!search || searchMatches(datesTitle, search));

            this.dateViewWrapper.$el.toggleClass('hide', this.hideDates);
            this.dateViewWrapper.toggle(this.filterModel.get('text') || !this.collapsed.dates);
        },

        updateIndexesVisibility: function() {
            this.indexesViewWrapper.$el.toggleClass('hide', this.indexesEmpty);

            this.indexesViewWrapper.toggle(this.filterModel.get('text') || !this.collapsed.indexes);
        }
    });

});
