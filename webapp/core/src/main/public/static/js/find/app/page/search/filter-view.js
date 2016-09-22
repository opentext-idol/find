/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/page/search/abstract-section-view',
    'find/app/page/search/filters/date/dates-filter-view',
    'find/app/page/search/filters/parametric/parametric-view',
    'find/app/page/search/filters/parametric/numeric-parametric-field-view',
    'find/app/util/text-input',
    'find/app/util/collapsible',
    'find/app/util/filtering-collection',
    'parametric-refinement/prettify-field-name',
    'parametric-refinement/display-collection',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'find/app/util/merge-collection'
], function(Backbone, $, _, AbstractSectionView, DateView, ParametricView, NumericParametricFieldView,
            TextInput, Collapsible, FilteringCollection, prettifyFieldName, ParametricDisplayCollection, configuration, i18n, i18nIndexes, MergeCollection) {
    'use strict';

    var datesTitle = i18n['search.dates'];

    var createFilteringCollection = function(baseCollection, filterModel) {
        return new FilteringCollection([], {
            collection: baseCollection,
            filterModel: filterModel,
            predicate: filterPredicate,
            resetOnFilter: false
        });
    };

    function filterPredicate(model, filterModel) {
        var searchText = filterModel && filterModel.get('text');
        return searchText ? searchMatches(prettifyFieldName(model.id), filterModel.get('text')) : true;
    }

    function searchMatches(text, search) {
        return text.toLowerCase().indexOf(search.toLowerCase()) > -1;
    }

    return AbstractSectionView.extend({
        initialize: function(options) {
            AbstractSectionView.prototype.initialize.apply(this, arguments);

            const IndexesView = options.IndexesView;
            this.collapsed = {};

            var views = [{
                shown: configuration().enableMetaFilter,
                initialize: function() {
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
                get$els: function() {
                    return [this.filterInput.$el, this.$emptyMessage];
                }.bind(this),
                render: function() {
                    this.filterInput.render();
                }.bind(this),
                postRender: function() {
                    this.updateParametricVisibility();
                    this.updateDatesVisibility();
                    this.updateIndexesVisibility();
                    this.updateEmptyMessage();
                }.bind(this),
                remove: function() {
                    this.filterInput.remove();
                }.bind(this)
            }, {
                shown: true,
                initialize: function() {
                    this.indexesEmpty = false;
                    this.collapsed.indexes = false;

                    //noinspection JSUnresolvedFunction
                    var indexesView = new IndexesView({
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
                get$els: function() {
                    return [this.indexesViewWrapper.$el];
                }.bind(this),
                render: function() {
                    this.indexesViewWrapper.render();
                }.bind(this),
                postRender: $.noop,
                remove: function() {
                    this.indexesViewWrapper.remove();
                }.bind(this)
            }, {
                shown: true,
                initialize: function() {
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
                get$els: function() {
                    return [this.dateViewWrapper.$el];
                }.bind(this),
                render: function() {
                    this.dateViewWrapper.render();
                }.bind(this),
                postRender: $.noop,
                remove: function() {
                    this.dateViewWrapper.remove();
                }.bind(this)
            }, {
                shown: true,
                initialize: function() {
                    this.parametricDisplayCollection = new ParametricDisplayCollection([], {
                        parametricCollection: options.parametricCollection,
                        restrictedParametricCollection: options.restrictedParametricCollection,
                        selectedParametricValues: options.queryState.selectedParametricValues,
                        filterModel: this.filterModel
                    });
                    this.mergedParametricCollection = new MergeCollection([], {
                        comparator: function(model) {
                            console.log(model.get('displayName'));
                            return model.get('displayName');
                        },
                        collections: [this.numericParametricFieldsCollection, this.dateParametricFieldsCollection, this.parametricDisplayCollection],
                        typeAttribute: 'dataType'
                    });

                    if(this.filterModel) {
                        //noinspection JSUnresolvedFunction
                        this.listenTo(this.mergedParametricCollection, 'update reset', function () {
                            this.updateParametricVisibility();
                            this.updateEmptyMessage();
                        });
                    }

                    this.parametricView = new ParametricView({
                        filterModel: this.filterModel,
                        queryModel: options.queryModel,
                        queryState: options.queryState,
                        timeBarModel: options.timeBarModel,
                        collection: this.mergedParametricCollection,
                        inputTemplate: NumericParametricFieldView.dateInputTemplate,
                        formatting: NumericParametricFieldView.dateFormatting,
                        indexesCollection: options.indexesCollection,
                        parametricCollection: options.parametricCollection,
                        restrictedParametricCollection: options.restrictedParametricCollection,
                        displayCollection: this.parametricDisplayCollection
                    });
                }.bind(this),
                get$els: function() {
                    return [this.parametricView.$el];
                }.bind(this),
                render: function() {
                    this.parametricView.render();
                }.bind(this),
                postRender: $.noop,
                remove: function() {
                    this.parametricView.remove();
                    this.numericParametricFieldsCollection.stopListening();
                    this.dateParametricFieldsCollection.stopListening();
                    this.parametricDisplayCollection.stopListening();
                    this.mergedParametricCollection.stopListening();
                }.bind(this)
            }];

            //noinspection JSUnresolvedFunction
            this.views = _.where(views, {shown: true});

            //noinspection JSUnresolvedFunction
            _.invoke(this.views, 'initialize');
        },

        render: function() {
            AbstractSectionView.prototype.render.apply(this, arguments);

            //noinspection JSUnresolvedVariable
            this.getViewContainer().empty();
            this.views.forEach(function(view) {
                view.get$els().forEach(function($el) {
                    //noinspection JSUnresolvedVariable
                    this.getViewContainer().append($el);
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

            AbstractSectionView.prototype.remove.call(this);
        },

        updateEmptyMessage: function() {
            var noFiltersMatched = !(this.indexesEmpty && this.hideDates && this.mergedParametricCollection.length === 0);

            this.$emptyMessage.toggleClass('hide', noFiltersMatched);
        },

        updateParametricVisibility: function() {
           
            var filterModelSwitch = Boolean(this.filterModel.get('text'));

            this.parametricView.$el.toggleClass('hide',
                this.mergedParametricCollection.length === 0 && filterModelSwitch);
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