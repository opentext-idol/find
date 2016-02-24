define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/model/dates-filter-model',
    'find/app/model/indexes-collection',
    'find/app/model/entity-collection',
    'find/app/model/query-model',
    'find/app/model/search-filters-collection',
    'find/app/page/search/filters/parametric/parametric-view',
    'find/app/model/parametric-collection',
    'find/app/page/search/filter-display/filter-display-view',
    'find/app/page/search/filters/date/dates-filter-view',
    'find/app/page/search/results/results-view-container',
    'find/app/page/search/results/results-view-selection',
    'find/app/page/search/related-concepts/related-concepts-view',
    'find/app/page/search/spellcheck-view',
    'find/app/util/collapsible',
    'find/app/util/model-any-changed-attribute-listener',
    'parametric-refinement/selected-values-collection',
    'find/app/page/search/saved-searches/saved-search-control-view',
    'find/app/page/search/results/topic-map-view',
    'find/app/page/search/results/sunburst-view',
    'find/app/page/search/compare-modal',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'text!find/templates/app/page/search/service-view.html'
], function(Backbone, $, _, DatesFilterModel, IndexesCollection, EntityCollection, QueryModel, SearchFiltersCollection,
            ParametricView, ParametricCollection, FilterDisplayView, DateView, ResultsViewContainer, ResultsViewSelection, RelatedConceptsView, SpellCheckView,
            Collapsible, addChangeListener, SelectedParametricValuesCollection, SavedSearchControlView, TopicMapView, SunburstView, CompareModal, i18n, i18nIndexes, template) {

    'use strict';

    var html = _.template(template)({i18n: i18n});

    var collapseView = function(title, view) {
        return new Collapsible({
            view: view,
            collapsed: false,
            title: title
        });
    };

    return Backbone.View.extend({
        className: 'full-height',

        // May be overridden
        SearchFiltersCollection: SearchFiltersCollection,

        // Abstract
        ResultsView: null,
        ResultsViewAugmentation: null,
        IndexesView: null,

        events: {
            'click .compare-modal-button': function() {
                new CompareModal({
                    savedSearchCollection: this.savedSearchCollection,
                    selectedSearch: this.savedSearchModel,
                    callback: _.bind(function(selectedCid) {
                        //TODO: call a compareSavedSearches() function here
                    }, this)
                });
            }
        },

        initialize: function(options) {
            this.indexesCollection = options.indexesCollection;
            this.savedSearchCollection = options.savedSearchCollection;
            this.savedSearchModel = options.savedSearchModel;
            this.queryState = options.queryState;
            this.documentsCollection = options.documentsCollection;

            this.entityCollection = new EntityCollection();

            this.queryModel = new QueryModel({}, {queryState: this.queryState});

            this.parametricCollection = new ParametricCollection();

            this.listenTo(this.queryModel, 'change:indexes', function() {
                this.queryState.selectedParametricValues.reset();
            });

            // There are 2 conditions where we want to reset the date we last fetched new docs on the date filter model

            // Either:
            //      We have a change in the query model that is not related to the date filters
            this.listenTo(this.queryModel, 'change', function(model) {
                if(!_.has(model.changed, 'minDate') && !_.has(model.changed, 'maxDate')) {
                    this.queryState.datesFilterModel.resetDateLastFetched();
                }
            });

            // Or:
            //      We have a change in the selected date filter (but not to NEW or from NEW to null)
            this.listenTo(this.queryState.datesFilterModel, 'change:dateRange', function(model, value) {
                var changeToNewDocFilter = value === DatesFilterModel.DateRange.NEW;
                var removeNewDocFilter = !value && model.previous('dateRange') === DatesFilterModel.DateRange.NEW;

                if(!changeToNewDocFilter && !removeNewDocFilter) {
                    this.queryState.datesFilterModel.resetDateLastFetched();
                }
            });

            this.filtersCollection = new this.SearchFiltersCollection([], {
                queryState: this.queryState,
                indexesCollection: this.indexesCollection
            });

            addChangeListener(this, this.queryModel, ['queryText', 'indexes', 'fieldText', 'minDate', 'maxDate'], this.fetchEntities);

            this.savedSearchControlView = new SavedSearchControlView({
                savedSearchModel: this.savedSearchModel,
                savedSearchCollection: this.savedSearchCollection,
                queryModel: this.queryModel,
                queryState: this.queryState
            });

            var constructorArguments = {
                documentsCollection: this.documentsCollection,
                entityCollection: this.entityCollection,
                indexesCollection: this.indexesCollection,
                queryModel: this.queryModel,
                queryTextModel: this.queryState.queryTextModel,
                parametricCollection: this.parametricCollection
            };

            this.resultsViewAugmentation = new this.ResultsViewAugmentation({
                resultsView: new this.ResultsView(constructorArguments)
            });

            var resultsViews = [{
                content: this.resultsViewAugmentation,
                id: 'list',
                uniqueId: _.uniqueId('results-view-item-'),
                selector: {
                    displayNameKey: 'list',
                    icon: 'hp-list'
                }
            }, {
                content: new TopicMapView(constructorArguments),
                id: 'topic-map',
                uniqueId: _.uniqueId('results-view-item-'),
                selector: {
                    displayNameKey: 'topic-map',
                    icon: 'hp-grid'
                }
            }, {
                content: new SunburstView(constructorArguments),
                id: 'sunburst',
                uniqueId: _.uniqueId('results-view-item-'),
                selector: {
                    displayNameKey: 'sunburst',
                    icon: 'hp-favorite'
                }
            }];

            this.listenTo(this.resultsViewAugmentation, 'rightSideContainerHideToggle' , function(toggle) {
                this.rightSideContainerHideToggle(toggle);
            }, this);

            var selectionModel = new Backbone.Model({
                // ID of the currently selected tab
                selectedTab: resultsViews[0].id
            });

            this.resultsViewSelection = new ResultsViewSelection({
                views: resultsViews,
                model: selectionModel
            });

            this.resultsViewContainer = new ResultsViewContainer({
                views: resultsViews,
                model: selectionModel
            });

            // Left Views
            this.filterDisplayView = new FilterDisplayView({
                collection: this.filtersCollection
            });

            this.parametricView = new ParametricView({
                queryModel: this.queryModel,
                queryState: this.queryState,
                parametricCollection: this.parametricCollection
            });

            // Left Collapsed Views
            this.indexesView = new this.IndexesView({
                queryModel: this.queryModel,
                indexesCollection: this.indexesCollection,
                selectedDatabasesCollection: this.queryState.selectedIndexes
            });

            this.dateView = new DateView({
                datesFilterModel: this.queryState.datesFilterModel,
                savedSearchModel: this.savedSearchModel
            });

            //Right Collapsed View
            this.relatedConceptsView = new RelatedConceptsView({
                entityCollection: this.entityCollection,
                indexesCollection: this.indexesCollection,
                queryModel: this.queryModel,
                queryTextModel: this.queryState.queryTextModel
            });

            this.spellCheckView = new SpellCheckView({
                documentsCollection: this.documentsCollection,
                queryModel: this.queryModel
            });

            // Collapse wrappers
            this.indexesViewWrapper = collapseView(i18nIndexes['search.indexes'], this.indexesView);
            this.dateViewWrapper = collapseView(i18n['search.dates'], this.dateView);
            this.relatedConceptsViewWrapper = collapseView(i18n['search.relatedConcepts'], this.relatedConceptsView);

            this.listenTo(this.savedSearchCollection, 'reset update', this.updateCompareModalButton);
        },

        render: function() {
            this.$el.html(html);

            this.filterDisplayView.setElement(this.$('.filter-display-container')).render();
            this.savedSearchControlView.setElement(this.$('.search-options-container')).render();
            this.indexesViewWrapper.setElement(this.$('.indexes-container')).render();
            this.parametricView.setElement(this.$('.parametric-container')).render();
            this.dateViewWrapper.setElement(this.$('.date-container')).render();
            this.spellCheckView.setElement(this.$('.spellcheck-container')).render();

            this.relatedConceptsViewWrapper.render();

            this.$('.related-concepts-container').append(this.relatedConceptsViewWrapper.$el);

            this.resultsViewSelection.setElement(this.$('.results-view-selection')).render();
            this.resultsViewContainer.setElement(this.$('.results-view-container')).render();

            this.$('.container-toggle').on('click', this.containerToggle);

            this.updateCompareModalButton();
            this.fetchEntities();
        },

        updateCompareModalButton: function() {
            this.$('.compare-modal-button').toggleClass('disabled not-clickable', this.savedSearchCollection.length <= 1);
        },

        fetchEntities: function() {
            if (this.queryModel.get('queryText') && this.queryModel.get('indexes').length !== 0) {
                this.entityCollection.fetch({
                    data: {
                        databases: this.queryModel.get('indexes'),
                        queryText: this.queryModel.get('queryText'),
                        fieldText: this.queryModel.get('fieldText'),
                        minDate: this.queryModel.getIsoDate('minDate'),
                        maxDate: this.queryModel.getIsoDate('maxDate')
                    }
                });
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
        }
    });

});
