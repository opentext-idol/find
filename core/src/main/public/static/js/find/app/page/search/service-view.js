/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'underscore',
    'moment',
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
    'find/app/util/collapsible',
    'find/app/util/model-any-changed-attribute-listener',
    'find/app/page/search/saved-searches/saved-search-control-view',
    'find/app/page/search/results/entity-topic-map-view',
    'find/app/page/search/results/sunburst-view',
    'find/app/page/search/results/map-results-view',
    'find/app/page/search/results/table/table-view',
    'find/app/page/search/time-bar-view',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/service-view.html'
], function(Backbone, $, _, moment, DatesFilterModel, EntityCollection, QueryModel, SavedSearchModel, ParametricCollection, 
            ParametricFieldsCollection, queryStrategy, stateTokenStrategy, ResultsViewContainer, ResultsViewSelection, 
            RelatedConceptsView, Collapsible, addChangeListener, SavedSearchControlView, TopicMapView, SunburstView, 
            MapResultsView, TableView, TimeBarView, configuration, i18n, templateString) {

    'use strict';

    var $window = $(window);
    var template = _.template(templateString);

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
        headerControlsHtml: '',
        displayDependentParametricViews: true,

        // Abstract
        ResultsView: null,
        ResultsViewAugmentation: null,
        fetchParametricFields: null,
        fetchParametricValues: null,

        initialize: function(options) {
            var hasBiRole = configuration().hasBiRole;

            this.indexesCollection = options.indexesCollection;
            this.selectedTabModel = options.selectedTabModel;
            this.savedSearchCollection = options.savedSearchCollection;
            this.savedSearchModel = options.savedSearchModel;
            this.queryState = options.queryState;
            this.documentsCollection = options.documentsCollection;
            this.searchTypes = options.searchTypes;
            this.searchCollections = options.searchCollections;

            this.highlightModel = new Backbone.Model({highlightEntities: false});

            this.entityCollection = new EntityCollection([], {
                getSelectedRelatedConcepts: function() {
                    return _.flatten(this.queryState.queryTextModel.get('relatedConcepts')).concat([this.queryState.queryTextModel.get('inputText')]);
                }.bind(this)
            });

            var searchType = this.savedSearchModel.get('type');

            this.queryModel = new QueryModel({
                autoCorrect: this.searchTypes[searchType].autoCorrect,
                stateMatchIds: this.savedSearchModel.get('queryStateTokens'),
                promotionsStateMatchIds: this.savedSearchModel.get('promotionsStateTokens')
            }, {queryState: this.queryState});

            this.listenTo(this.queryModel, 'change:indexes', function() {
                this.queryState.selectedParametricValues.reset();
            });

            this.listenTo(this.savedSearchModel, 'refresh', function() {
                this.queryModel.trigger('refresh');
            });

            // There are 2 conditions where we want to reset the date we last fetched new docs on the date filter model

            // Either:
            //      We have a change in the query model that is not related to the date filters
            this.listenTo(this.queryModel, 'change', function(model) {
                if (!_.has(model.changed, 'minDate') && !_.has(model.changed, 'maxDate')) {
                    this.queryState.datesFilterModel.resetDateLastFetched();
                }
            });

            // Or:
            //      We have a change in the selected date filter (but not to NEW or from NEW to null)
            this.listenTo(this.queryState.datesFilterModel, 'change:dateRange', function(model, value) {
                var changeToNewDocFilter = value === DatesFilterModel.DateRange.NEW;
                var removeNewDocFilter = !value && model.previous('dateRange') === DatesFilterModel.DateRange.NEW;

                if (!changeToNewDocFilter && !removeNewDocFilter) {
                    this.queryState.datesFilterModel.resetDateLastFetched();
                }
            });

            // If the saved search is unmodified and not new, update the last fetched date
            this.listenTo(this.documentsCollection, 'sync', function() {
                var changed = this.queryState ? !this.savedSearchModel.equalsQueryState(this.queryState) : false;

                if (!changed && !this.savedSearchModel.isNew()) {
                    this.savedSearchModel.save({dateDocsLastFetched: moment()});
                }
            });

            this.parametricFieldsCollection = new ParametricFieldsCollection([], {url: '../api/public/fields/parametric'});
            this.restrictedParametricCollection = new ParametricCollection([], {url: '../api/public/parametric/restricted'});
            this.numericParametricFieldsCollection = new ParametricFieldsCollection([], {url: '../api/public/fields/parametric-numeric'});
            this.dateParametricFieldsCollection = new ParametricFieldsCollection([], {url: '../api/public/fields/parametric-date'});
            this.parametricCollection = new ParametricCollection([], {url: '../api/public/parametric'});

            var subViewArguments = {
                indexesCollection: this.indexesCollection,
                entityCollection: this.entityCollection,
                savedSearchModel: this.savedSearchModel,
                savedSearchCollection: this.savedSearchCollection,
                documentsCollection: this.documentsCollection,
                selectedTabModel: this.selectedTabModel,
                parametricCollection: this.parametricCollection,
                restrictedParametricCollection: this.restrictedParametricCollection,                
                parametricFieldsCollection: this.parametricFieldsCollection,
                numericParametricFieldsCollection: this.numericParametricFieldsCollection,
                dateParametricFieldsCollection: this.dateParametricFieldsCollection,
                queryModel: this.queryModel,
                queryState: this.queryState,
                highlightModel: this.highlightModel,
                searchCollections: this.searchCollections,
                searchTypes: this.searchTypes
            };

            var clickHandlerArguments = {
                queryTextModel: this.queryState.queryTextModel,
                savedQueryCollection: this.searchCollections.QUERY,
                savedSearchModel: this.savedSearchModel,
                selectedTabModel: this.selectedTabModel
            };

            if (hasBiRole) {
                this.savedSearchControlView = new SavedSearchControlView(subViewArguments);

                if (this.searchTypes[searchType].showTimeBar) {
                    this.timeBarView = new TimeBarView(subViewArguments);
                }
            }

            this.leftSideFooterView = new this.searchTypes[searchType].LeftSideFooterView(subViewArguments);

            var MiddleColumnHeaderView = this.searchTypes[searchType].MiddleColumnHeaderView;
            this.middleColumnHeaderView = MiddleColumnHeaderView ? new MiddleColumnHeaderView(subViewArguments) : null;

            var relatedConceptsClickHandler = this.searchTypes[searchType].relatedConceptsClickHandler(clickHandlerArguments);

            var relatedConceptsView = new RelatedConceptsView(_.extend({
                clickHandler: relatedConceptsClickHandler,
                highlightModel: this.highlightModel
            }, subViewArguments));

            this.relatedConceptsViewWrapper = new Collapsible({
                view: relatedConceptsView,
                collapsed: false,
                title: i18n['search.relatedConcepts']
            });

            this.middleColumnScrollModel = new Backbone.Model();

            var resultsView = new this.ResultsView(_.defaults({
                enablePreview: true,
                relatedConceptsClickHandler: relatedConceptsClickHandler,
                fetchStrategy: this.searchTypes[searchType].fetchStrategy,
                highlightModel: this.highlightModel,
                scrollModel: this.middleColumnScrollModel
            }, subViewArguments));

            this.resultsViews = _.where([{
                Constructor: this.ResultsViewAugmentation,
                id: 'list',
                shown: true,
                uniqueId: _.uniqueId('results-view-item-'),
                constructorArguments: {
                    resultsView: resultsView,
                    queryModel: this.queryModel,
                    scrollModel: this.middleColumnScrollModel
                },
                events: {
                    // needs binding as the view container will be the eventual listener
                    'rightSideContainerHideToggle': _.bind(this.rightSideContainerHideToggle, this)
                },
                selector: {
                    displayNameKey: 'list',
                    icon: 'hp-list'
                }
            }, {
                Constructor: TopicMapView,
                id: 'topic-map',
                shown: hasBiRole,
                uniqueId: _.uniqueId('results-view-item-'),
                constructorArguments: _.extend({
                    clickHandler: relatedConceptsClickHandler
                }, subViewArguments),
                selector: {
                    displayNameKey: 'topic-map',
                    icon: 'hp-grid'
                }
            }, {
                Constructor: SunburstView,
                constructorArguments: subViewArguments,
                id: 'sunburst',
                shown: hasBiRole && this.displayDependentParametricViews,
                uniqueId: _.uniqueId('results-view-item-'),
                selector: {
                    displayNameKey: 'sunburst',
                    icon: 'hp-favorite'
                }
            }, {
                Constructor: MapResultsView,
                id: 'map',
                shown: hasBiRole && configuration().map.enabled,
                uniqueId: _.uniqueId('results-view-item-'),
                constructorArguments: _.extend({
                    resultsStep: this.mapViewResultsStep,
                    allowIncrement: this.mapViewAllowIncrement
                }, subViewArguments),
                selector: {
                    displayNameKey: 'map',
                    icon: 'hp-map-view'
                }
            }, {
                Constructor: TableView,
                constructorArguments: subViewArguments,
                id: 'table',
                shown: hasBiRole && this.displayDependentParametricViews,
                uniqueId: _.uniqueId('results-view-item-'),
                selector: {
                    displayNameKey: 'table',
                    icon: 'hp-table'
                }
            }], {shown: true});

            var resultsViewSelectionModel = new Backbone.Model({
                // ID of the currently selected tab
                selectedTab: this.resultsViews[0].id
            });

            // need a selector if multiple active views
            if (this.resultsViews.length > 1) {
                this.resultsViewSelection = new ResultsViewSelection({
                    views: this.resultsViews,
                    model: resultsViewSelectionModel
                });
            }

            this.resultsViewContainer = new ResultsViewContainer({
                views: this.resultsViews,
                model: resultsViewSelectionModel
            });

            this.listenTo(this.queryModel, 'refresh', this.fetchData);
            this.listenTo(this.queryModel, 'change', this.fetchRestrictedParametricCollection);
            this.fetchParametricFields(this.parametricFieldsCollection, this.parametricCollection);
            this.fetchParametricFields(this.numericParametricFieldsCollection);
            this.fetchParametricFields(this.dateParametricFieldsCollection);
            this.fetchEntities();
            this.fetchRestrictedParametricCollection();

            this.updateScrollParameters = updateScrollParameters.bind(this);

            $window
                .scroll(this.updateScrollParameters)
                .resize(this.updateScrollParameters);
        },

        render: function() {
            var hasBiRole = configuration().hasBiRole;

            this.$el.html(template({
                i18n: i18n,
                headerControlsHtml: this.headerControlsHtml,
                hasBiRole: hasBiRole,
                showTimeBar: Boolean(this.timeBarView)
            }));

            if (this.timeBarView) {
                this.timeBarView.setElement(this.$('.middle-container-time-bar')).render();
            }

            if (this.savedSearchControlView) {
                // the padding looks silly if we don't have the view so add it here
                var $searchOptionContainer = this.$('.search-options-container').addClass('p-sm');

                this.savedSearchControlView.setElement($searchOptionContainer).render();
            }

            this.relatedConceptsViewWrapper.render();

            this.$('.related-concepts-container').append(this.relatedConceptsViewWrapper.$el);

            if (this.resultsViewSelection) {
                this.resultsViewSelection.setElement(this.$('.results-view-selection')).render();
            }

            this.resultsViewContainer.setElement(this.$('.results-view-container')).render();

            this.leftSideFooterView.setElement(this.$('.left-side-footer')).render();

            if (this.middleColumnHeaderView) {
                this.middleColumnHeaderView.setElement(this.$('.middle-column-header')).render();
            }

            this.$('.container-toggle').on('click', this.containerToggle);

            this.$middleContainerContents = this.$('.middle-container-contents').scroll(this.updateScrollParameters);
            this.updateScrollParameters();
        },

        fetchData: function() {
            this.fetchEntities();
            this.fetchParametricValues(this.parametricFieldsCollection, this.parametricCollection);
        },

        fetchEntities: function() {
            if (this.queryModel.get('queryText') && this.queryModel.get('indexes').length !== 0) {
                var data = {
                    databases: this.queryModel.get('indexes'),
                    queryText: this.queryModel.get('queryText'),
                    fieldText: this.queryModel.get('fieldText'),
                    minDate: this.queryModel.getIsoDate('minDate'),
                    maxDate: this.queryModel.getIsoDate('maxDate'),
                    minScore: this.queryModel.get('minScore'),
                    stateTokens: this.queryModel.get('stateMatchIds')
                };

                this.entityCollection.fetch({data: data});
            }
        },

        containerToggle: function(event) {
            var $containerToggle = $(event.currentTarget);
            var $sideContainer = $containerToggle.closest('.side-container');
            var hide = !$sideContainer.hasClass('small-container');

            $sideContainer.find('.side-panel-content').toggleClass('hide', hide);
            $sideContainer.toggleClass('small-container', hide);
            $containerToggle.toggleClass('fa-rotate-180', hide);
        },

        fetchRestrictedParametricCollection: function() {
            this.restrictedParametricCollection.fetch({
                data: {
                    fieldNames: this.parametricFieldsCollection.pluck('id'),
                    databases: this.queryModel.get('indexes'),
                    queryText: this.queryModel.get('queryText'),
                    fieldText: this.queryModel.get('fieldText'),
                    minDate: this.queryModel.getIsoDate('minDate'),
                    maxDate: this.queryModel.getIsoDate('maxDate'),
                    minScore: this.queryModel.get('minScore'),
                    stateTokens: this.queryModel.get('stateMatchIds')
                }
            });
        },

        rightSideContainerHideToggle: function(toggle) {
            this.$('.right-side-container').toggle(toggle);
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
                this.relatedConceptsViewWrapper,
                this.leftSideFooterView,
                this.middleColumnHeaderView,
                this.timeBarView
            ])
                .compact()
                .invoke('remove');

            Backbone.View.prototype.remove.call(this);
        }
    });

});
