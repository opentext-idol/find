/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'find/app/model/geography-model',
    'find/app/page/search/abstract-section-view',
    'find/app/page/search/filters/date/dates-filter-view',
    'find/app/page/search/filters/geography/geography-view',
    'find/app/page/search/filters/parametric/filtered-parametric-fields-collection',
    'find/app/page/search/filters/parametric/parametric-view',
    'find/app/page/search/filters/parametric/numeric-parametric-field-view',
    'find/app/util/text-input',
    'find/app/util/collapsible',
    'find/app/util/filtering-collection',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'text!find/templates/app/page/search/filters/filter-separator.html'
], function(_, $, Backbone, GeographyModel, AbstractSectionView, DateView, GeographyView, FilteredParametricFieldsCollection,
            ParametricView, NumericParametricFieldView, TextInput, Collapsible, FilteringCollection,
            configuration, i18n, i18nIndexes, filterSeparator) {
    'use strict';

    const datesTitle = i18n['search.dates'];
    const geographyTitle = i18n['search.geography'];

    function searchMatches(text, search) {
        return text.toLowerCase().indexOf(search.toLowerCase()) > -1;
    }

    const showGeographyFilter = GeographyModel.LocationFields.length > 0;

    return AbstractSectionView.extend({
        initialize: function(options) {
            AbstractSectionView.prototype.initialize.apply(this, arguments);

            const IndexesView = options.IndexesView;
            this.collapsed = {};

            const config = configuration();

            const views = [{
                id: 'metaFilter',
                shown: config.enableMetaFilter,
                initialize: function() {
                    //Initializing the text with empty string to stop IE11 issue with triggering input event on render
                    this.filterModel = new Backbone.Model({text: ''});

                    this.filterInput = new TextInput({
                        model: this.filterModel,
                        modelAttribute: 'text',
                        templateOptions: {
                            placeholder: i18n['search.filters.filter']
                        }
                    });

                    this.$emptyMessage = $('<p class="hide">' + i18n['search.filters.empty'] + '</p>');

                    this.listenTo(this.filterModel, 'change', function() {
                        this.updateDatesVisibility();
                        this.updateGeographyVisibility();
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
                    this.updateGeographyVisibility();
                    this.updateIndexesVisibility();
                    this.updateEmptyMessage();
                }.bind(this),
                remove: function() {
                    this.filterInput.remove();
                }.bind(this)
            }, {
                id: 'indexesFilter',
                shown: true,
                initialize: function() {
                    this.indexesEmpty = false;
                    this.collapsed.indexes = true;

                    const indexesView = new IndexesView({
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
                        collapseModel: new Backbone.Model({collapsed: this.collapsed.indexes}),
                        title: i18nIndexes['search.indexes']
                    });

                    // only track user triggered changes, not automatic ones
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
                postRender: _.noop,
                remove: function() {
                    this.indexesViewWrapper.remove();
                }.bind(this)
            }, {
                id: 'datesFilter',
                shown: true,
                initialize: function() {
                    this.collapsed.dates = true;

                    const dateView = new DateView({
                        datesFilterModel: options.queryState.datesFilterModel,
                        savedSearchModel: options.savedSearchModel
                    });

                    this.dateViewWrapper = new Collapsible({
                        view: dateView,
                        collapseModel: new Backbone.Model({collapsed: this.collapsed.dates}),
                        title: datesTitle
                    });

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
                postRender: _.noop,
                remove: function() {
                    this.dateViewWrapper.remove();
                }.bind(this)
            }, {
                id: 'geographyFilter',
                shown: showGeographyFilter,
                initialize: function() {
                    const geographyModel = options.queryState.geographyModel;
                    this.collapsed.geography = !_.find(_.map(geographyModel.attributes, function(v){ return v && v.length }));

                    const geographyView = new GeographyView({
                        geographyModel: geographyModel,
                        savedSearchModel: options.savedSearchModel
                    })

                    this.geographyViewWrapper = new Collapsible({
                        view: geographyView,
                        collapseModel: new Backbone.Model({collapsed: this.collapsed.geography}),
                        title: geographyTitle
                    })

                    this.listenTo(this.geographyViewWrapper, 'toggle', function(newState) {
                        this.collapsed.geography = newState;
                    });
                }.bind(this),
                get$els: function() {
                    return [this.geographyViewWrapper.$el];
                }.bind(this),
                render: function() {
                    this.geographyViewWrapper.render();
                }.bind(this),
                postRender: _.noop,
                remove: function() {
                    this.geographyViewWrapper.remove();
                }.bind(this)
            }, {
                id: 'parametricFilter',
                shown: true,
                initialize: function() {
                    this.parametricFieldsCollection = options.parametricFieldsCollection;
                    const filteredParametricCollection = new FilteringCollection([], {
                        collection: options.parametricCollection,
                        predicate: _.constant(true)
                    });
                    this.filteredParametricFieldsCollection = new FilteredParametricFieldsCollection([], {
                        collection: this.parametricFieldsCollection,
                        filterModel: this.filterModel,
                        queryModel: options.queryModel,
                        parametricCollection: options.parametricCollection,
                        filteredParametricCollection: filteredParametricCollection
                    });

                    if(this.filterModel) {
                        this.listenTo(this.filteredParametricFieldsCollection, 'update reset', function() {
                            this.updateParametricVisibility();
                            this.updateEmptyMessage();
                        });
                    }

                    this.parametricView = new ParametricView({
                        filterModel: this.filterModel,
                        queryModel: options.queryModel,
                        queryState: options.queryState,
                        timeBarModel: options.timeBarModel,
                        collection: this.filteredParametricFieldsCollection,
                        parametricFieldsCollection: this.parametricFieldsCollection,
                        inputTemplate: NumericParametricFieldView.dateInputTemplate,
                        formatting: NumericParametricFieldView.dateFormatting,
                        indexesCollection: options.indexesCollection,
                        filteredParametricCollection: filteredParametricCollection
                    });
                }.bind(this),
                get$els: function() {
                    return [this.parametricView.$el];
                }.bind(this),
                render: function() {
                    this.parametricView.render();
                }.bind(this),
                postRender: _.noop,
                remove: function() {
                    this.parametricView.remove();
                }.bind(this)
            }];

            if (config && config.uiCustomization && config.uiCustomization.filterOrder) {
                const unused = views.splice(0);

                _.each(config.uiCustomization.filterOrder, function(id){
                    if (id === '-') {
                        const $el = $(filterSeparator);
                        views.push({
                            shown: true,
                            initialize: _.noop,
                            get$els: function() {
                                return [$el];
                            }.bind(this),
                            render: _.noop,
                            postRender: _.noop,
                            remove: function() {
                                $el.remove();
                            }.bind(this)
                        })
                    }
                    else {
                        const viewIdx = _.findIndex(unused, function(view){ return view.id === id });
                        if (viewIdx >= 0) {
                            views.push(unused.splice(viewIdx, 1)[0]);
                        }
                    }
                });

                views.push.apply(views, unused);
            }

            this.views = _.where(views, {shown: true});
            _.invoke(this.views, 'initialize');
        },

        render: function() {
            AbstractSectionView.prototype.render.apply(this);

            this.getViewContainer().empty();
            this.views.forEach(function(view) {
                view.get$els().forEach(function($el) {
                    this.getViewContainer().append($el);
                }.bind(this));
            }.bind(this));

            _.invoke(this.views, 'render');
            _.invoke(this.views, 'postRender');

            return this;
        },

        remove: function() {
            _.invoke(this.views, 'remove');

            AbstractSectionView.prototype.remove.call(this);
        },

        updateEmptyMessage: function() {
            const noFiltersMatched = !(
                this.indexesEmpty &&
                this.hideDates &&
                this.parametricFieldsEmpty()
            );

            this.$emptyMessage.toggleClass('hide', noFiltersMatched);
        },

        updateParametricVisibility: function() {
            this.parametricView.$el.toggleClass('hide',
                this.parametricFieldsEmpty() && !!(this.filterModel.get('text')));
        },

        updateDatesVisibility: function() {
            const search = this.filterModel.get('text');
            this.hideDates = !(!search || searchMatches(datesTitle, search));

            this.dateViewWrapper.$el.toggleClass('hide', this.hideDates);
            this.dateViewWrapper.toggle(this.filterModel.get('text') || !this.collapsed.dates);
        },

        updateGeographyVisibility: function() {
            if (!this.geographyViewWrapper) {
                return;
            }

            const search = this.filterModel.get('text');
            this.hideGeography = !(!search || searchMatches(geographyTitle, search));

            this.geographyViewWrapper.$el.toggleClass('hide', this.hideGeography);
            this.geographyViewWrapper.toggle(this.filterModel.get('text') || !this.collapsed.geography);
        },

        updateIndexesVisibility: function() {
            this.indexesViewWrapper.$el.toggleClass('hide', this.indexesEmpty);
            this.indexesViewWrapper.toggle(this.filterModel.get('text') || !this.collapsed.indexes);
        },

        parametricFieldsEmpty: function() {
            return !this.filteredParametricFieldsCollection || this.filteredParametricFieldsCollection.isEmpty();
        }
    });
});
