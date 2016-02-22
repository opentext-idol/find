define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/model/dates-filter-model',
    'find/app/model/indexes-collection',
    'find/app/model/entity-collection',
    'find/app/model/query-model',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/model/search-filters-collection',
    'find/app/page/search/filters/parametric/parametric-view',
    'find/app/model/parametric-collection',
    'find/app/page/search/filter-display/filter-display-view',
    'find/app/page/search/filters/date/dates-filter-view',
    'find/app/page/search/results/results-view-augmentation',
    'find/app/page/search/results/results-view-container',
    'find/app/page/search/results/results-view-selection',
    'find/app/page/search/related-concepts/related-concepts-view',
    'find/app/page/search/spellcheck-view',
    'find/app/page/search/snapshots/snapshot-data-view',
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
], function(Backbone, $, _, DatesFilterModel, IndexesCollection, EntityCollection, QueryModel, SavedSearchModel, SearchFiltersCollection,
            ParametricView, ParametricCollection, FilterDisplayView, DateView, ResultsViewAugmentation, ResultsViewContainer, ResultsViewSelection, RelatedConceptsView, SpellCheckView,
            SnapshotDataView, Collapsible, addChangeListener, SelectedParametricValuesCollection, SavedSearchControlView, TopicMapView, SunburstView, CompareModal, i18n, i18nIndexes, template) {

    'use strict';

    // Invoke the method on each item in the list if it exists
    function safeInvoke(method, list) {
        return _.map(list, function(item) {
            return item && item[method]();
        });
    }

    var collapseView = function(title, view) {
        return new Collapsible({
            view: view,
            collapsed: false,
            title: title
        });
    };

    return Backbone.View.extend({
        className: 'full-height',
        template: _.template(template),

        // May be overridden
        SearchFiltersCollection: SearchFiltersCollection,

        // Abstract
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
            this.selectedTabModel = options.selectedTabModel;
            this.savedSearchCollection = options.savedSearchCollection;
            this.savedSearchModel = options.savedSearchModel;
            this.savedSnapshotCollection = options.savedSnapshotCollection;
            this.savedQueryCollection = options.savedQueryCollection;
            this.queryState = options.queryState;
            this.documentsCollection = options.documentsCollection;

            this.entityCollection = new EntityCollection();

            var searchType = this.savedSearchModel.get('type');

            this.queryModel = new QueryModel({
                autoCorrect: searchType === SavedSearchModel.Type.QUERY
            }, {queryState: this.queryState});

            this.listenTo(this.queryModel, 'change:indexes', function() {
                this.queryState.selectedParametricValues.reset();
            });

            addChangeListener(this, this.queryModel, ['queryText', 'indexes', 'fieldText'], this.fetchEntities);
            
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
                documentsCollection: this.documentsCollection,
                savedSearchModel: this.savedSearchModel,
                savedSearchCollection: this.savedSearchCollection,
                savedSnapshotCollection: this.savedSnapshotCollection,
                savedQueryCollection: this.savedQueryCollection,
                selectedTabModel: this.selectedTabModel,
                queryModel: this.queryModel,
                queryState: this.queryState
            });

            var relatedConceptsView = new RelatedConceptsView({
                entityCollection: this.entityCollection,
                indexesCollection: this.indexesCollection,
                queryModel: this.queryModel,
                queryTextModel: this.queryState.queryTextModel
            });

            this.relatedConceptsViewWrapper = collapseView(i18n['search.relatedConcepts'], relatedConceptsView);
            
            if (searchType === SavedSearchModel.Type.QUERY) {
                var parametricCollection = new ParametricCollection();
                
                var resultsViewConstructorArguments = {
                    documentsCollection: this.documentsCollection,
                    entityCollection: this.entityCollection,
                    indexesCollection: this.indexesCollection,
                    parametricCollection: parametricCollection,
                    queryModel: this.queryModel,
                    queryTextModel: this.queryState.queryTextModel
                };

                this.resultsViewAugmentation = new this.ResultsViewAugmentation(resultsViewConstructorArguments);
                this.topicMapView = new TopicMapView(resultsViewConstructorArguments);
                this.sunburstView = new SunburstView(constructorArguments);

                var resultsViews = [{
                    content: this.resultsViewAugmentation,
                    id: 'list',
                    uniqueId: _.uniqueId('results-view-item-'),
                    selector: {
                        displayNameKey: 'list',
                        icon: 'hp-list'
                    }
                }, {
                    content: this.topicMapView,
                    id: 'topic-map',
                    uniqueId: _.uniqueId('results-view-item-'),
                    selector: {
                        displayNameKey: 'topic-map',
                        icon: 'hp-grid'
                    }
                }, {
                    content: this.sunburstView,
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

                var resultsViewSelectionModel = new Backbone.Model({
                    // ID of the currently selected tab
                    selectedTab: resultsViews[0].id
                });

                this.resultsViewSelection = new ResultsViewSelection({
                    views: resultsViews,
                    model: resultsViewSelectionModel
                });

                this.resultsViewContainer = new ResultsViewContainer({
                    views: resultsViews,
                    model: resultsViewSelectionModel
                });

                this.spellCheckView = new SpellCheckView({
                    documentsCollection: this.documentsCollection,
                    queryModel: this.queryModel
                });

                this.indexesView = new this.IndexesView({
                    queryModel: this.queryModel,
                    indexesCollection: this.indexesCollection,
                    selectedDatabasesCollection: this.queryState.selectedIndexes
                });

                this.dateView = new DateView({
                    datesFilterModel: this.queryState.datesFilterModel,
                    savedSearchModel: this.savedSearchModel
                });

                this.parametricView = new ParametricView({
                    queryModel: this.queryModel,
                    queryState: this.queryState,
                    indexesCollection: this.indexesCollection
                });

                this.filtersCollection = new this.SearchFiltersCollection([], {
                    queryState: this.queryState,
                    indexesCollection: this.indexesCollection
                });

                this.filterDisplayView = new FilterDisplayView({collection: this.filtersCollection});

                this.indexesViewWrapper = collapseView(i18nIndexes['search.indexes'], this.indexesView);
                this.dateViewWrapper = collapseView(i18n['search.dates'], this.dateView);
            } else if (searchType === SavedSearchModel.Type.SNAPSHOT) {
                this.snapshotDataView = new SnapshotDataView({
                    savedSearchModel: this.savedSearchModel
                });
            }

            this.listenTo(this.savedSearchCollection, 'reset update', this.updateCompareModalButton);
        },

        render: function() {
            var searchType = this.savedSearchModel.get('type');

            this.$el.html(this.template({
                i18n: i18n,
                searchType: searchType,
                SearchType: SavedSearchModel.Type
            }));

            this.savedSearchControlView.setElement(this.$('.search-options-container')).render();
            this.relatedConceptsViewWrapper.render();

            this.$('.related-concepts-container').append(this.relatedConceptsViewWrapper.$el);

            this.$('.container-toggle').on('click', this.containerToggle);

            if (searchType === SavedSearchModel.Type.QUERY) {
                this.filterDisplayView.setElement(this.$('.filter-display-container')).render();
                this.indexesViewWrapper.setElement(this.$('.indexes-container')).render();
                this.parametricView.setElement(this.$('.parametric-container')).render();
                this.dateViewWrapper.setElement(this.$('.date-container')).render();
                this.spellCheckView.setElement(this.$('.spellcheck-container')).render();
                this.resultsViewSelection.setElement(this.$('.results-view-selection')).render();
                this.resultsViewContainer.setElement(this.$('.results-view-container')).render();
            } else if (searchType === SavedSearchModel.Type.SNAPSHOT) {
                // TODO: Replace with a state token results view
                this.snapshotDataView.setElement(this.$('.snapshot-view-container')).render();
                this.$('.state-token-placeholder').text(this.savedSearchModel.get('stateTokens')[0] || 'None');
            }

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
        },

        remove: function() {
            safeInvoke('stopListening', [
                this.queryModel,
                this.filtersCollection
            ]);

            safeInvoke('remove', [
                this.savedSearchControlView,
                this.resultsViewAugmentation,
                this.topicMapView,
                this.resultsViewContainer,
                this.resultsViewSelection,
                this.relatedConceptsViewWrapper,
                this.spellCheckView,
                this.parametricView,
                this.filterDisplayView,
                this.snapshotDataView,
                this.indexesViewWrapper,
                this.dateViewWrapper
            ]);

            Backbone.View.prototype.remove.call(this);
        }
    });

});
