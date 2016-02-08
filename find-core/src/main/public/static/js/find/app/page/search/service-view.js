define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/model/dates-filter-model',
    'find/app/model/documents-collection',
    'find/app/model/indexes-collection',
    'find/app/model/entity-collection',
    'find/app/model/query-model',
    'find/app/model/search-filters-collection',
    'find/app/page/search/filters/parametric/parametric-view',
    'find/app/page/search/filter-display/filter-display-view',
    'find/app/page/search/filters/date/dates-filter-view',
    'find/app/page/search/results/results-view-augmentation',
    'find/app/page/search/results/results-view-container',
    'find/app/page/search/results/results-view-selection',
    'find/app/page/search/related-concepts/related-concepts-view',
    'find/app/page/search/spellcheck-view',
    'find/app/util/collapsible',
    'find/app/util/model-any-changed-attribute-listener',
    'parametric-refinement/selected-values-collection',
    'find/app/page/search/saved-searches/saved-search-control-view',
    'find/app/page/search/results/topic-map-view',
    'find/app/page/search/compare-modal',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'text!find/templates/app/page/search/service-view.html'
], function(Backbone, $, _, DatesFilterModel, DocumentsCollection, IndexesCollection, EntityCollection, QueryModel, SearchFiltersCollection,
            ParametricView, FilterDisplayView, DateView, ResultsViewAugmentation, ResultsViewContainer, ResultsViewSelection, RelatedConceptsView, SpellCheckView,
            Collapsible, addChangeListener, SelectedParametricValuesCollection, SavedSearchControlView, TopicMapView, CompareModal, i18n, i18nIndexes, template) {

    'use strict';

    var html = _.template(template)({i18n: i18n});

    function selectInitialIndexes(indexesCollection) {
        var privateIndexes = indexesCollection.reject({domain: 'PUBLIC_INDEXES'});
        var selectedIndexes;

        if (privateIndexes.length > 0) {
            selectedIndexes = privateIndexes;
        } else {
            selectedIndexes = indexesCollection.models;
        }

        return _.map(selectedIndexes, function(indexModel) {
            return indexModel.pick('domain', 'name');
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
        className: 'inline',

        // May be overridden
        SearchFiltersCollection: SearchFiltersCollection,

        // Abstract
        ResultsView: null,
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

            this.documentsCollection = new DocumentsCollection();
            this.entityCollection = new EntityCollection();

            var initialSelectedIndexes;
            var savedSelectedIndexes = this.savedSearchModel.toSelectedIndexes();

            // TODO: Check if the saved indexes still exists?
            if (savedSelectedIndexes.length === 0) {
                if (this.indexesCollection.isEmpty()) {
                    initialSelectedIndexes = [];

                    this.listenToOnce(this.indexesCollection, 'sync', function() {
                        this.queryState.selectedIndexes.set(selectInitialIndexes(this.indexesCollection));
                    });
                } else {
                    initialSelectedIndexes = selectInitialIndexes(this.indexesCollection);
                }
            } else {
                initialSelectedIndexes = savedSelectedIndexes;
            }

            this.queryState = {
                queryTextModel: options.queryTextModel,
                datesFilterModel: new DatesFilterModel(this.savedSearchModel.toDatesFilterModelAttributes()),
                selectedParametricValues: new SelectedParametricValuesCollection(this.savedSearchModel.toSelectedParametricValues()),
                selectedIndexes: new IndexesCollection(initialSelectedIndexes)
            };

            this.queryModel = new QueryModel({}, {queryState: this.queryState});

            this.listenTo(this.queryModel, 'change:indexes', function() {
                this.queryState.selectedParametricValues.reset();
            });

            this.filtersCollection = new this.SearchFiltersCollection([], {
                queryState: this.queryState,
                indexesCollection: this.indexesCollection
            });

            addChangeListener(this, this.queryModel, ['queryText', 'indexes', 'fieldText'], this.fetchEntities);

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
                queryTextModel: this.queryState.queryTextModel
            };

            this.resultsViewAugmentation = new ResultsViewAugmentation({
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
                queryState: this.queryState
            });

            // Left Collapsed Views
            this.indexesView = new this.IndexesView({
                queryModel: this.queryModel,
                indexesCollection: this.indexesCollection,
                selectedDatabasesCollection: this.queryState.selectedIndexes
            });

            this.dateView = new DateView({
                queryModel: this.queryModel,
                datesFilterModel: this.queryState.datesFilterModel
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
