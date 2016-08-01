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

    var createFilteringCollection = function(baseCollection, filterModel) {
        return new FilteringCollection([], {
            collection: baseCollection,
            filterModel: filterModel,
            predicate: filterPredicate,
            resetOnFilter: false
        });
    };

    function filterPredicate(filterModel, model) {
        var searchText = filterModel && filterModel.get('text');
        return searchText ? searchMatches(prettifyFieldName(model.id), filterModel.get('text')) : true;
    }

    function searchMatches(text, search) {
        return text.toLowerCase().indexOf(search.toLowerCase()) > -1;
    }

    return Backbone.View.extend({
        // Abstract
        IndexesView: null,

        initialize: function(options) {
            this.collapsed = {};

            var views = [{
                shown: configuration().enableMetaFilter,
                initialize: function () {
                    this.filterModel = new Backbone.Model();

                    this.filterInput = new TextInput({
                        model: this.filterModel,
                        modelAttribute: 'text',
                        templateOptions: {
                            placeholder: i18n['search.filters.filter']
                        }
                    });

                    this.$emptyMessage = $('<p class="hide">' + i18n['search.filters.empty'] + '</p>');

                    //noinspection JSUnresolvedFunction
                    this.listenTo(this.filterModel, 'change', function() {
                        this.updateDatesVisibility();
                        this.updateParametricVisibility();
                        this.updateEmptyMessage();
                    });
                }.bind(this),
                get$els: function () {
                    return [this.filterInput.$el, this.$emptyMessage];
                }.bind(this),
                render: function () {
                    this.filterInput.render();
                }.bind(this),
                postRender: function () {
                    this.updateParametricVisibility();
                    this.updateDatesVisibility();
                    this.updateIndexesVisibility();
                    this.updateEmptyMessage();
                }.bind(this),
                remove: function () {
                    this.filterInput.remove();
                }.bind(this)
            }, {
                shown: true,
                initialize: function () {
                    this.indexesEmpty = false;
                    this.collapsed.indexes = false;

                    //noinspection JSUnresolvedFunction
                    var indexesView = new this.IndexesView({
                        delayedSelection: options.delayedIndexesSelection,
                        filterModel: this.filterModel,
                        indexesCollection: options.indexesCollection,
                        queryModel: options.queryModel,
                        selectedDatabasesCollection: options.queryState.selectedIndexes,
                        visibleIndexesCallback: _.bind(function(indexes) {
                            this.indexesEmpty = indexes.length === 0;
                            this.updateIndexesVisibility();
                            this.updateEmptyMessage();
                        }, this)
                    });

                    this.indexesViewWrapper = new Collapsible({
                        view: indexesView,
                        collapsed: this.collapsed.indexes,
                        title: i18nIndexes['search.indexes']
                    });

                    // only track user triggered changes, not automatic ones
                    //noinspection JSUnresolvedFunction
                    this.listenTo(this.indexesViewWrapper, 'toggle', function(newState) {
                        this.collapsed.indexes = newState;
                    });
                }.bind(this),
                get$els: function () {
                    return [this.indexesViewWrapper.$el];
                }.bind(this),
                render: function () {
                    this.indexesViewWrapper.render();
                }.bind(this),
                postRender: $.noop,
                remove: function () {
                    this.indexesViewWrapper.remove();
                }.bind(this)
            }, {
                shown: true,
                initialize: function () {
                    this.collapsed.dates = false;

                    var dateView = new DateView({
                        datesFilterModel: options.queryState.datesFilterModel,
                        savedSearchModel: options.savedSearchModel
                    });

                    this.dateViewWrapper = new Collapsible({
                        view: dateView,
                        collapsed: this.collapsed.dates,
                        title: datesTitle
                    });

                    //noinspection JSUnresolvedFunction
                    this.listenTo(this.dateViewWrapper, 'toggle', function(newState) {
                        this.collapsed.dates = newState;
                    });
                }.bind(this),
                get$els: function () {
                    return [this.dateViewWrapper.$el];
                }.bind(this),
                render: function () {
                    this.dateViewWrapper.render();
                }.bind(this),
                postRender: $.noop,
                remove: function () {
                    this.dateViewWrapper.remove();
                }.bind(this)
            }, {
                shown: true,
                initialize: function () {
                    this.numericParametricFieldsCollection = options.numericParametricFieldsCollection;
                    this.filteredNumericCollection = createFilteringCollection(this.numericParametricFieldsCollection, this.filterModel);

                    this.numericParametricView = new NumericParametricView({
                        filterModel: this.filterModel,
                        queryModel: options.queryModel,
                        queryState: options.queryState,
                        timeBarModel: options.timeBarModel,
                        dataType: 'numeric',
                        collection: this.filteredNumericCollection,
                        numericRestriction: true
                    });
                }.bind(this),
                get$els: function () {
                    return [this.numericParametricView.$el];
                }.bind(this),
                render: function () {
                    this.numericParametricView.render();
                }.bind(this),
                postRender: $.noop,
                remove: function () {
                    this.numericParametricView.remove();
                }.bind(this)
            }, {
                shown: true,
                initialize: function () {
                    this.dateParametricFieldsCollection = options.dateParametricFieldsCollection;
                    this.filteredDateCollection = createFilteringCollection(this.dateParametricFieldsCollection, this.filterModel);

                    this.dateParametricView = new NumericParametricView({
                        filterModel: this.filterModel,
                        queryModel: options.queryModel,
                        queryState: options.queryState,
                        timeBarModel: options.timeBarModel,
                        dataType: 'date',
                        collection: this.filteredDateCollection,
                        inputTemplate: NumericParametricFieldView.dateInputTemplate,
                        formatting: NumericParametricFieldView.dateFormatting
                    });
                }.bind(this),
                get$els: function () {
                    return [this.dateParametricView.$el];
                }.bind(this),
                render: function () {
                    this.dateParametricView.render();
                }.bind(this),
                postRender: $.noop,
                remove: function () {
                    this.dateParametricView.remove();
                }.bind(this)
            }, {
                shown: true,
                initialize: function () {
                    this.parametricDisplayCollection = new ParametricDisplayCollection([], {
                        parametricCollection: options.restrictedParametricCollection,
                        selectedParametricValues: options.queryState.selectedParametricValues,
                        filterModel: this.filterModel
                    });

                    if (this.filterModel) {
                        //noinspection JSUnresolvedFunction
                        this.listenTo(this.parametricDisplayCollection, 'update reset', function () {
                            this.updateParametricVisibility();
                            this.updateEmptyMessage();
                        });
                    }

                    this.parametricView = new ParametricView({
                        queryModel: options.queryModel,
                        queryState: options.queryState,
                        filterModel: this.filterModel,
                        indexesCollection: options.indexesCollection,
                        parametricCollection: options.parametricCollection,
                        restrictedParametricCollection: options.restrictedParametricCollection,
                        displayCollection: this.parametricDisplayCollection
                    });
                }.bind(this),
                get$els: function () {
                    return [this.parametricView.$el];
                }.bind(this),
                render: function () {
                    this.parametricView.render();
                }.bind(this),
                postRender: $.noop,
                remove: function () {
                    this.parametricView.remove();
                }.bind(this)
            }];

            //noinspection JSUnresolvedFunction
            this.views = _.where(views, {shown: true});

            //noinspection JSUnresolvedFunction
            _.invoke(this.views, 'initialize');
        },

        render: function() {
            //noinspection JSUnresolvedVariable
            this.$el.empty();
            this.views.forEach(function (view) {
                view.get$els().forEach(function ($el) {
                    //noinspection JSUnresolvedVariable
                    this.$el.append($el);
                }.bind(this));
            }.bind(this));

            //noinspection JSUnresolvedFunction
            _.invoke(this.views, 'render');
            //noinspection JSUnresolvedFunction
            _.invoke(this.views, 'postRender');

            return this;
        },

        remove: function() {
            //noinspection JSUnresolvedFunction
            _.invoke(this.views, 'remove');

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
