/*
 * Copyright 2016-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'underscore',
    'moment',
    'find/app/metrics',
    'find/app/model/dates-filter-model',
    'find/app/model/entity-collection',
    'find/app/model/query-model',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/model/parametric-collection',
    'find/app/model/parametric-fields-collection',
    'find/app/page/search/results/query-strategy',
    'find/app/page/search/results/state-token-strategy',
    'find/app/util/results-view-container',
    'find/app/util/results-view-selection',
    'find/app/page/search/related-concepts/related-concepts-view',
    'js-whatever/js/model-any-changed-attribute-listener',
    'find/app/page/search/saved-searches/saved-search-control-view',
    'find/app/page/search/results/entity-topic-map-view',
    'find/app/page/search/results/sunburst-view',
    'find/app/page/search/results/map-results-view',
    'find/app/page/search/results/dategraph/dategraph-view',
    'find/app/page/search/results/table/table-view',
    'find/app/page/search/time-bar-view',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/service-view.html'
], function (Backbone, $, _, moment, metrics, DatesFilterModel, EntityCollection, QueryModel, SavedSearchModel,
             ParametricCollection, ParametricFieldsCollection,
             queryStrategy, stateTokenStrategy, ResultsViewContainer, ResultsViewSelection,
             RelatedConceptsView, addChangeListener, SavedSearchControlView, TopicMapView,
             SunburstView, MapResultsView, DateGraphView, TableView, TimeBarView, configuration, i18n, templateString) {
    'use strict';

    const $window = $(window);
    const template = _.template(templateString);

    function updateScrollParameters() {
        if (this.$middleContainerContents) {
            this.middleColumnScrollModel.set({
                innerHeight: this.$middleContainerContents.innerHeight(),
                scrollTop: this.$middleContainerContents.scrollTop(),
                scrollHeight: this.$middleContainerContents.prop('scrollHeight'),
                top: this.$middleContainerContents.get(0).getBoundingClientRect().top,
                bottom: this.$middleContainerContents.get(0).getBoundingClientRect().bottom
            });
        }
    }

    return Backbone.View.extend({
        // Can be overridden
        displayDependentParametricViews: true,

        getSavedSearchControlViewOptions: function () {
            return {};
        },

        // Abstract
        ResultsView: null,
        ResultsViewAugmentation: null,
        fetchParametricFields: null,

        timeBarView: null,

        initialize: function (options) {
            const hasBiRole = configuration().hasBiRole;

            this.indexesCollection = options.indexesCollection;
            this.selectedTabModel = options.selectedTabModel;
            this.savedSearchCollection = options.savedSearchCollection;
            this.savedSearchModel = options.savedSearchModel;
            this.queryState = options.queryState;
            this.documentsCollection = options.documentsCollection;
            this.searchTypes = options.searchTypes;
            this.searchCollections = options.searchCollections;

            const searchType = this.savedSearchModel.get('type');

            this.queryModel = new QueryModel({
                autoCorrect: this.searchTypes[searchType].autoCorrect,
                stateMatchIds: this.savedSearchModel.get('queryStateTokens'),
                promotionsStateMatchIds: this.savedSearchModel.get('promotionsStateTokens')
            }, {queryState: this.queryState});

            this.listenTo(this.savedSearchModel, 'refresh', function () {
                this.queryModel.trigger('refresh');
            });

            // There are 2 conditions where we want to reset the date we last fetched new docs on the date filter model

            // Either:
            //      We have a change in the query model that is not related to the date filters
            this.listenTo(this.queryModel, 'change', function (model) {
                if (!_.has(model.changed, 'minDate') && !_.has(model.changed, 'maxDate')) {
                    this.queryState.datesFilterModel.resetDateLastFetched();
                }
            });

            // Or:
            //      We have a change in the selected date filter (but not to NEW or from NEW to null)
            this.listenTo(this.queryState.datesFilterModel, 'change:dateRange', function (model, value) {
                const changeToNewDocFilter = value === DatesFilterModel.DateRange.NEW;
                const removeNewDocFilter = !value && model.previous('dateRange') === DatesFilterModel.DateRange.NEW;

                if (!changeToNewDocFilter && !removeNewDocFilter) {
                    this.queryState.datesFilterModel.resetDateLastFetched();
                }
            });

            // If the saved search is unmodified and not new, update the last fetched date
            this.listenTo(this.documentsCollection, 'sync', function () {
                const changed = this.queryState ? !this.savedSearchModel.equalsQueryState(this.queryState) : false;

                if (!changed && !this.savedSearchModel.isNew()) {
                    this.savedSearchModel.save({dateDocsLastFetched: moment()});
                }
            });

            this.parametricFieldsCollection = new ParametricFieldsCollection([]);
            this.parametricCollection = new ParametricCollection([], {url: 'api/public/parametric/values'});

            // Tracks the document model which is currently shown in the preview
            this.previewModeModel = new Backbone.Model({document: null});

            const subViewArguments = {
                configuration: configuration(),
                dateParametricFieldsCollection: this.dateParametricFieldsCollection,
                delayedIndexesSelection: options.delayedIndexesSelection,
                documentsCollection: this.documentsCollection,
                indexesCollection: this.indexesCollection,
                highlightModel: this.highlightModel,
                numericParametricFieldsCollection: this.numericParametricFieldsCollection,
                queryModel: this.queryModel,
                queryState: this.queryState,
                parametricCollection: this.parametricCollection,
                parametricFieldsCollection: this.parametricFieldsCollection,
                previewModeModel: this.previewModeModel,
                savedSearchCollection: this.savedSearchCollection,
                savedSearchModel: this.savedSearchModel,
                searchCollections: this.searchCollections,
                searchTypes: this.searchTypes,
                selectedTabModel: this.selectedTabModel
            };

            const clickHandlerArguments = {
                conceptGroups: this.queryState.conceptGroups,
                savedQueryCollection: this.searchCollections.QUERY,
                savedSearchModel: this.savedSearchModel,
                selectedTabModel: this.selectedTabModel
            };

            if (hasBiRole) {
                this.savedSearchControlView = new SavedSearchControlView(_.extend(this.getSavedSearchControlViewOptions(), subViewArguments));

                if (this.searchTypes[searchType].showTimeBar) {
                    this.timeBarModel = new Backbone.Model({});

                    this.listenTo(this.timeBarModel, 'change:graphedFieldId', this.updateTimeBar);
                }
            }

            this.leftSideFooterView = new this.searchTypes[searchType].LeftSideFooterView(_.extend({timeBarModel: this.timeBarModel}, subViewArguments));

            const MiddleColumnHeaderView = this.searchTypes[searchType].MiddleColumnHeaderView;
            this.middleColumnHeaderView = MiddleColumnHeaderView ? new MiddleColumnHeaderView(subViewArguments) : null;

            const relatedConceptsClickHandler = this.searchTypes[searchType]
                .relatedConceptsClickHandler(clickHandlerArguments);

            // TODO: genericise removal of feature (FIND-245)
            if (configuration().enableRelatedConcepts) {
                this.entityCollection = new EntityCollection([], {
                    getSelectedRelatedConcepts: function () {
                        return _.flatten(this.queryState.conceptGroups.pluck('concepts'));
                    }.bind(this)
                });
                this.relatedConceptsView = new RelatedConceptsView(_.extend({
                    entityCollection: this.entityCollection,
                    clickHandler: relatedConceptsClickHandler
                }, subViewArguments));
                this.fetchEntities();
            }

            this.middleColumnScrollModel = new Backbone.Model();

            const resultsView = new this.ResultsView(_.defaults({
                relatedConceptsClickHandler: relatedConceptsClickHandler,
                fetchStrategy: this.searchTypes[searchType].fetchStrategy,
                scrollModel: this.middleColumnScrollModel
            }, subViewArguments));

            //noinspection JSUnresolvedVariable
            const resultsViewsMap = {
                'topic-map': {
                    Constructor: TopicMapView,
                    shown: hasBiRole,
                    constructorArguments: _.extend({
                        clickHandler: relatedConceptsClickHandler,
                        type: 'QUERY'
                    }, subViewArguments),
                    selector: {
                        displayNameKey: 'topic-map',
                        icon: 'hp-grid'
                    }
                },
                list: {
                    Constructor: this.ResultsViewAugmentation,
                    shown: true,
                    constructorArguments: {
                        resultsView: resultsView,
                        queryModel: this.queryModel,
                        indexesCollection: this.indexesCollection,
                        previewModeModel: this.previewModeModel,
                        scrollModel: this.middleColumnScrollModel,
                        mmapTab: options.mmapTab
                    },
                    events: {
                        // needs binding as the view container will be the eventual listener
                        'rightSideContainerHideToggle': _.bind(this.rightSideContainerHideToggle, this)
                    },
                    selector: {
                        displayNameKey: 'list',
                        icon: 'hp-list'
                    }
                },
                sunburst: {
                    Constructor: SunburstView,
                    constructorArguments: subViewArguments,
                    shown: hasBiRole && this.displayDependentParametricViews,
                    selector: {
                        displayNameKey: 'sunburst',
                        icon: 'hp-favorite'
                    }
                },
                map: {
                    Constructor: MapResultsView,
                    shown: hasBiRole && configuration().map.enabled,
                    constructorArguments: _.extend({
                        resultsStep: this.mapViewResultsStep,
                        allowIncrement: this.mapViewAllowIncrement
                    }, subViewArguments),
                    selector: {
                        displayNameKey: 'map',
                        icon: 'hp-map-view'
                    }
                },
                table: {
                    Constructor: TableView,
                    constructorArguments: subViewArguments,
                    shown: hasBiRole && this.displayDependentParametricViews,
                    selector: {
                        displayNameKey: 'table',
                        icon: 'hp-table'
                    }
                },
                dategraph: {
                    Constructor: DateGraphView,
                    constructorArguments: _.extend({
                        timeBarModel: this.timeBarModel
                    }, subViewArguments),
                    shown: hasBiRole,
                    selector: {
                        displayNameKey: 'dategraph',
                        icon: 'hp-analytics'
                    }
                }
            };

            this.resultsViews = configuration().resultViewOrder
                .filter(function (viewId) {
                    return resultsViewsMap[viewId] && resultsViewsMap[viewId].shown;
                })
                .map(function (viewId) {
                    return _.extend({
                        id: viewId,
                        uniqueId: _.uniqueId('results-view-item-')
                    }, resultsViewsMap[viewId]);
                });

            this.resultsViewSelectionModel = new Backbone.Model({
                // ID of the currently selected tab
                selectedTab: this.resultsViews[0].id
            });

            // need a selector if multiple active views
            if (this.resultsViews.length > 1) {
                this.resultsViewSelection = new ResultsViewSelection({
                    views: this.resultsViews,
                    model: this.resultsViewSelectionModel
                });

                this.listenTo(this.queryModel.queryState.selectedParametricValues, 'graph', function(){
                    this.resultsViewSelection.switchTab('dategraph')
                })
            }

            this.resultsViewContainer = new ResultsViewContainer({
                views: this.resultsViews,
                model: this.resultsViewSelectionModel
            });

            this.listenTo(this.resultsViewSelectionModel, 'change:selectedTab', function(model, selectedTab) {
                this.trigger('updateRouting', selectedTab);
            });

            this.listenTo(this.queryModel, 'refresh', this.fetchData);

            addChangeListener(this, this.queryModel, ['correctedQuery', 'autoCorrect'], function (model) {
                if (model.get('correctedQuery') || !model.get('autoCorrect')) {
                    this.fetchData();
                }
            }.bind(this));

            this.listenForParametricFieldMetrics();

            this.fetchParametricFields(this.parametricFieldsCollection, this.fetchParametricCollection.bind(this));

            this.updateScrollParameters = updateScrollParameters.bind(this);

            $window
                .scroll(this.updateScrollParameters)
                .resize(this.updateScrollParameters);
        },

        render: function () {
            const hasBiRole = configuration().hasBiRole;

            this.$el.html(template({
                relatedConcepts: configuration().enableRelatedConcepts,
                hasBiRole: hasBiRole
            }));

            this.$middleContainer = this.$('.middle-container');
            this.renderTimeBar();

            if (this.savedSearchControlView) {
                // the padding looks silly if we don't have the view so add it here
                const $searchOptionContainer = this.$('.search-options-container').addClass('p-sm');

                this.savedSearchControlView.setElement($searchOptionContainer).render();
            }

            // TODO: genericise removal of feature (FIND-245)
            if (configuration().enableRelatedConcepts) {
                this.relatedConceptsView.render();
                this.$('.related-concepts-container').append(this.relatedConceptsView.$el);
            }

            if (this.resultsViewSelection) {
                this.resultsViewSelection.setElement(this.$('.results-view-selection')).render();
            }

            this.resultsViewContainer.setElement(this.$('.results-view-container')).render();

            this.leftSideFooterView.setElement(this.$('.left-side-footer')).render();

            if (this.middleColumnHeaderView) {
                this.middleColumnHeaderView.setElement(this.$('.middle-column-header')).render();
            }

            this.$('.container-toggle').click('click', _.bind(this.containerToggle, this));

            this.$middleContainerContents = this.$('.middle-container-contents').scroll(this.updateScrollParameters);
            this.updateScrollParameters();
        },

        update: function () {
            this.resultsViewContainer.updateTab();
        },

        renderTimeBar: function () {
            if (this.timeBarView && this.$middleContainer) {
                this.$middleContainer.append(this.timeBarView.$el);
                this.timeBarView.render();
            }
        },

        updateTimeBar: function () {
            const graphedFieldId = this.timeBarModel.get('graphedFieldId');
            const collapsed = graphedFieldId === null;

            if (this.$middleContainer) {
                this.$middleContainer.toggleClass('middle-container-with-time-bar', !collapsed);
            }

            if (this.timeBarView) {
                this.timeBarView.remove();
                this.timeBarView = null;
            }

            if (!collapsed) {
                this.timeBarView = new TimeBarView({
                    queryModel: this.queryModel,
                    queryState: this.queryState,
                    previewModeModel: this.previewModeModel,
                    timeBarModel: this.timeBarModel,
                    parametricFieldsCollection: this.parametricFieldsCollection
                });

                this.renderTimeBar();
            }
        },

        fetchData: function () {
            if (this.entityCollection) {
                this.fetchEntities();
            }

            this.fetchParametricCollection();
        },

        fetchEntities: function () {
            if (this.queryModel.get('queryText') && this.queryModel.get('indexes').length !== 0) {
                const data = {
                    databases: this.queryModel.get('indexes'),
                    queryText: this.queryModel.get('autoCorrect') && this.queryModel.get('correctedQuery')
                        ? this.queryModel.get('correctedQuery')
                        : this.queryModel.get('queryText'),
                    fieldText: this.queryModel.get('fieldText'),
                    minDate: this.queryModel.getIsoDate('minDate'),
                    maxDate: this.queryModel.getIsoDate('maxDate'),
                    minScore: this.queryModel.get('minScore'),
                    stateTokens: this.queryModel.get('stateMatchIds')
                };

                this.entityCollection.fetch({data: data});
            }
        },

        containerToggle: function (event) {
            const $containerToggle = $(event.currentTarget);
            const $sideContainer = $containerToggle.closest('.side-container');
            const hide = !$sideContainer.hasClass('small-container');

            $sideContainer.find('.side-panel-content').toggleClass('hide', hide);
            $sideContainer.toggleClass('small-container', hide);
            $containerToggle.toggleClass('fa-rotate-180', hide);
            this.resultsViewContainer.updateTab();
        },

        listenForParametricFieldMetrics: function () {
            if (metrics.enabled()) {
                //noinspection JSUnresolvedFunction
                this.listenTo(this.parametricFieldsCollection, 'sync', function () {
                    if (!this.parametricFieldsCollection.isEmpty() && !this.parametricFieldsLoaded) {
                        this.parametricFieldsLoaded = true;
                        metrics.addTimeSincePageLoad('parametric-fields-first-loaded');
                    }
                });

                this.listenTo(this.parametricCollection, 'sync', function () {
                    if (!this.parametricCollection.isEmpty() && !this.parametricValuesLoaded) {
                        this.parametricValuesLoaded = true;
                        metrics.addTimeSincePageLoad('parametric-values-first-loaded');
                    }
                });
            }
        },

        fetchParametricCollection: function () {
            this.parametricCollection.reset();

            const fieldNames = _.pluck(this.parametricFieldsCollection.where({type: 'Parametric'}), 'id');

            if (fieldNames.length > 0 && this.queryModel.get('indexes').length !== 0) {
                this.parametricCollection.fetch({
                    data: {
                        fieldNames: fieldNames,
                        databases: this.queryModel.get('indexes'),
                        queryText: this.queryModel.get('autoCorrect') && this.queryModel.get('correctedQuery')
                            ? this.queryModel.get('correctedQuery')
                            : this.queryModel.get('queryText'),
                        fieldText: this.queryModel.get('fieldText'),
                        minDate: this.queryModel.getIsoDate('minDate'),
                        maxDate: this.queryModel.getIsoDate('maxDate'),
                        minScore: this.queryModel.get('minScore'),
                        maxValues: 5,
                        stateTokens: this.queryModel.get('stateMatchIds')
                    }
                });
            }
        },

        rightSideContainerHideToggle: function (toggle) {
            this.$('.right-side-container').toggle(toggle);
        },

        changeTab: function (tab) {
            this.resultsViewSelection.switchTab(tab);
        },

        getSelectedTab: function () {
            return this.resultsViewSelectionModel.get('selectedTab');
        },

        remove: function() {
            $window
                .off('resize', this.updateScrollParameters)
                .off('scroll', this.updateScrollParameters);

            this.queryModel.stopListening();

            _.chain([
                this.savedSearchControlView,
                this.resultsViewContainer,
                this.resultsViewSelection,
                this.relatedConceptsView,
                this.leftSideFooterView,
                this.middleColumnHeaderView,
                this.timeBarView
            ]).compact().invoke('remove');

            Backbone.View.prototype.remove.call(this);
        }
    });
});
