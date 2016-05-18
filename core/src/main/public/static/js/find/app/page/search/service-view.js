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
    'find/app/page/search/results/query-strategy',
    'find/app/page/search/results/state-token-strategy',
    'find/app/page/search/results/results-view-augmentation',
    'find/app/util/results-view-container',
    'find/app/util/results-view-selection',
    'find/app/page/search/related-concepts/related-concepts-view',
    'find/app/util/collapsible',
    'find/app/util/model-any-changed-attribute-listener',
    'find/app/page/search/saved-searches/saved-search-control-view',
    'find/app/page/search/results/entity-topic-map-view',
    'find/app/page/search/results/sunburst-view',
    'find/app/page/search/results/map-results-view',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/service-view.html'
], function(Backbone, $, _, moment, DatesFilterModel, EntityCollection, QueryModel, SavedSearchModel, ParametricCollection,
            queryStrategy, stateTokenStrategy, ResultsViewAugmentation, ResultsViewContainer,
            ResultsViewSelection, RelatedConceptsView, Collapsible, addChangeListener,  SavedSearchControlView, TopicMapView,
            SunburstView, MapResultsView, configuration, i18n, templateString) {

    'use strict';

    var template = _.template(templateString);

    return Backbone.View.extend({
        className: 'full-height-viewport',

        // Can be overridden
        headerControlsHtml: '',
        displaySunburst: true,

        // Abstract
        ResultsView: null,
        ResultsViewAugmentation: null,

        initialize: function(options) {
            this.indexesCollection = options.indexesCollection;
            this.selectedTabModel = options.selectedTabModel;
            this.savedSearchCollection = options.savedSearchCollection;
            this.savedSearchModel = options.savedSearchModel;
            this.queryState = options.queryState;
            this.documentsCollection = options.documentsCollection;
            this.searchTypes = options.searchTypes;
            this.searchCollections = options.searchCollections;

            this.highlightModel = new Backbone.Model({highlightEntities: false});
            this.entityCollection = new EntityCollection();

            var searchType = this.savedSearchModel.get('type');

            this.queryModel = new QueryModel({
                autoCorrect: this.searchTypes[searchType].autoCorrect,
                stateMatchIds: this.savedSearchModel.get('queryStateTokens'),
                promotionsStateMatchIds: this.savedSearchModel.get('promotionsStateTokens')
            }, {queryState: this.queryState});

            this.listenTo(this.queryModel, 'change:indexes', function() {
                this.queryState.selectedParametricValues.reset();
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
                if(!changed && !this.savedSearchModel.isNew()) {
                    this.savedSearchModel.save({dateDocsLastFetched: moment()});
                }
            });

            this.parametricCollection = new ParametricCollection();

            var subViewArguments = {
                indexesCollection: this.indexesCollection,
                entityCollection: this.entityCollection,
                savedSearchModel: this.savedSearchModel,
                savedSearchCollection: this.savedSearchCollection,
                documentsCollection: this.documentsCollection,
                selectedTabModel: this.selectedTabModel,
                parametricCollection: this.parametricCollection,
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

            if (configuration().hasBiRole) {
                this.savedSearchControlView = new SavedSearchControlView(subViewArguments);
            }

            this.leftSideFooterView = new this.searchTypes[searchType].LeftSideFooterView(subViewArguments);

            var MiddleColumnHeaderView = this.searchTypes[searchType].MiddleColumnHeaderView;
            this.middleColumnHeaderView = MiddleColumnHeaderView ? new MiddleColumnHeaderView(subViewArguments) : null;

            var entityClickHandler = this.searchTypes[searchType].entityClickHandler(clickHandlerArguments);

            var relatedConceptsView = new RelatedConceptsView(_.extend({
                clickHandler: this.searchTypes[searchType].relatedConceptsClickHandler(clickHandlerArguments),
                highlightModel: this.highlightModel
            }, subViewArguments));

            this.relatedConceptsViewWrapper = new Collapsible({
                view: relatedConceptsView,
                collapsed: false,
                title: i18n['search.relatedConcepts']
            });

            var resultsView = new this.ResultsView(_.defaults({
                enablePreview: true,
                entityClickHandler: entityClickHandler,
                fetchStrategy: this.searchTypes[searchType].fetchStrategy,
                highlightModel: this.highlightModel
            }, subViewArguments));

            var hasBiRole = configuration().hasBiRole;

            this.resultsViews = _.where([{
                Constructor: this.ResultsViewAugmentation,
                id: 'list',
                shown: true,
                uniqueId: _.uniqueId('results-view-item-'),
                constructorArguments: {
                    resultsView: resultsView,
                    queryModel: this.queryModel
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
                    clickHandler: entityClickHandler
                }, subViewArguments),
                selector: {
                    displayNameKey: 'topic-map',
                    icon: 'hp-grid'
                }
            }, {
                Constructor: SunburstView,
                constructorArguments: subViewArguments,
                id: 'sunburst',
                shown: hasBiRole && this.displaySunburst,
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

            addChangeListener(this, this.queryModel, ['queryText', 'indexes', 'fieldText', 'minDate', 'maxDate', 'stateMatchIds'], this.fetchData);
            this.fetchData();
        },

        render: function() {
            this.$el.html(template({
                i18n: i18n,
                headerControlsHtml: this.headerControlsHtml
            }));

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
        },

        fetchData: function() {
            this.parametricCollection.reset();

            if (this.queryModel.get('queryText') && this.queryModel.get('indexes').length !== 0) {
                var data = {
                    databases: this.queryModel.get('indexes'),
                    queryText: this.queryModel.get('queryText'),
                    fieldText: this.queryModel.get('fieldText'),
                    minDate: this.queryModel.getIsoDate('minDate'),
                    maxDate: this.queryModel.getIsoDate('maxDate'),
                    stateTokens: this.queryModel.get('stateMatchIds')
                };

                this.entityCollection.fetch({data: data});
                this.parametricCollection.fetch({data: data});
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

        rightSideContainerHideToggle: function(toggle) {
            this.$('.right-side-container').toggle(toggle);
        },

        remove: function() {
            this.queryModel.stopListening();

            _.chain([
                this.savedSearchControlView,
                this.resultsViewContainer,
                this.resultsViewSelection,
                this.relatedConceptsViewWrapper,
                this.leftSideFooterView,
                this.middleColumnHeaderView
            ])
                .compact()
                .invoke('remove');

            Backbone.View.prototype.remove.call(this);
        }
    });

});
