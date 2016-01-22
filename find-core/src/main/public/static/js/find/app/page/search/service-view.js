define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/model/dates-filter-model',
    'find/app/model/documents-collection',
    'find/app/model/indexes-collection',
    'find/app/model/entity-collection',
    'find/app/model/search-filters-collection',
    'find/app/page/search/filters/parametric/parametric-view',
    'find/app/page/search/filter-display/filter-display-view',
    'find/app/page/search/filters/date/dates-filter-view',
    'find/app/page/search/results/results-view-container',
    'find/app/page/search/related-concepts/related-concepts-view',
    'find/app/page/search/results/results-number-view',
    'find/app/page/search/spellcheck-view',
    'find/app/page/search/saved-search-control/saved-search-control-view',
    'find/app/page/search/saved-searches-view',
    'find/app/page/search/filters/indexes/indexes-view',
    'find/app/util/collapsible',
    'parametric-refinement/selected-values-collection',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'text!find/templates/app/page/search/service-view.html'
], function(Backbone, $, _, DatesFilterModel, DocumentsCollection, IndexesCollection, EntityCollection, SearchFiltersCollection,
            ParametricView, FilterDisplayView, DateView, ResultsViewContainer, RelatedConceptsView, ResultsNumberView, SpellCheckView,
            SavedSearchControlView, SavedSearchesView, IndexesView, Collapsible, SelectedParametricValuesCollection, i18n, i18n_indexes, template) {
    "use strict";

    var collapseView = function (title, view) {
        return new Collapsible({
            view: view,
            collapsed: false,
            title: title
        });
    };

    return Backbone.View.extend({
        template: _.template(template)({i18n: i18n}),

        // may be overridden
        constructSearchFiltersCollection: function (queryModel, datesFilterModel, indexesCollection, selectedIndexesCollection, selectedParametricValues) {
            return new SearchFiltersCollection([], {
                queryModel: queryModel,
                datesFilterModel: datesFilterModel,
                indexesCollection: indexesCollection,
                selectedIndexesCollection: selectedIndexesCollection,
                selectedParametricValues: selectedParametricValues
            });
        },

        // will be overridden
        constructIndexesView: function (queryModel, indexesCollection, selectedIndexesCollection) {
            return new IndexesView({
                queryModel: queryModel,
                indexesCollection: indexesCollection,
                selectedDatabasesCollection: selectedIndexesCollection
            });
        },

        // will be overridden
        constructResultsViewContainer: null,

        initialize: function(options) {
            this.queryModel = options.queryModel;
            this.queryTextModel = options.queryTextModel;

            this.datesFilterModel = new DatesFilterModel({}, {queryModel: this.queryModel});

            this.documentsCollection = new DocumentsCollection();
            this.indexesCollection = new IndexesCollection();
            this.entityCollection = new EntityCollection();
            this.selectedParametricValues = new SelectedParametricValuesCollection();
            this.selectedIndexesCollection = new IndexesCollection();

            this.filtersCollection = this.constructSearchFiltersCollection(this.queryModel, this.datesFilterModel, this.indexesCollection, this.selectedIndexesCollection, this.selectedParametricValues);

            this.indexesCollection.fetch();

            var fetchEntities = _.bind(function() {
                if (this.queryModel.get('queryText') && this.queryModel.get('indexes').length !== 0) {
                    this.entityCollection.fetch({
                        data: {
                            queryText: this.queryModel.get('queryText'),
                            databases: this.queryModel.get('indexes'),
                            fieldText: this.queryModel.get('fieldText'),
                            minDate: this.queryModel.getIsoDate('minDate'),
                            maxDate: this.queryModel.getIsoDate('maxDate')
                        }
                    });
                }
            }, this);

            this.listenTo(this.queryTextModel, 'refresh', fetchEntities);

            this.listenTo(this.queryModel, 'change', function() {
                if (this.queryModel.hasAnyChangedAttributes(['queryText', 'indexes', 'fieldText'])) {
                    fetchEntities();
                }
            });

            this.listenTo(this.selectedIndexesCollection, 'update reset', _.debounce(_.bind(function() {
                this.queryModel.set('indexes', this.selectedIndexesCollection.map(function(model) {
                    return model.get('domain') ? encodeURIComponent(model.get('domain')) + ':' + encodeURIComponent(model.get('name')) : encodeURIComponent(model.get('name'));
                }));
            }, this), 500));

            this.savedSearchControlView = new SavedSearchControlView({
                queryModel: this.queryModel
            });

            this.savedSearchesView = new SavedSearchesView({
                savedSearchesCollection: new Backbone.Collection()
            });
            
            this.resultsViewContainer = this.constructResultsViewContainer({
                documentsCollection: this.documentsCollection,
                entityCollection: this.entityCollection,
                indexesCollection: this.indexesCollection,
                queryModel: this.queryModel,
                queryTextModel: this.queryTextModel
            });

            // Left Views
            this.filterDisplayView = new FilterDisplayView({
                collection: this.filtersCollection,
                datesFilterModel: this.datesFilterModel
            });

            this.parametricView = new ParametricView({
                queryModel: this.queryModel,
                selectedParametricValues: this.selectedParametricValues,
                indexesCollection: this.indexesCollection,
                selectedIndexesCollection: this.selectedIndexesCollection
            });

            // Left Collapsed Views
            this.indexesView = this.constructIndexesView(this.queryModel, this.indexesCollection, this.selectedIndexesCollection);

            this.dateView = new DateView({
                queryModel: this.queryModel,
                datesFilterModel: this.datesFilterModel
            });

            //Right Collapsed View
            this.relatedConceptsView = new RelatedConceptsView({
                entityCollection: this.entityCollection,
                indexesCollection: this.indexesCollection,
                queryModel: this.queryModel,
                queryTextModel: this.queryTextModel
            });

            this.resultsNumberView = new ResultsNumberView({
                documentsCollection: this.documentsCollection
            });

            this.spellCheckView = new SpellCheckView({
                documentsCollection: this.documentsCollection,
                queryModel: this.queryModel
            });

            // Collapse wrappers
            this.indexesViewWrapper = collapseView(i18n_indexes['search.indexes'], this.indexesView);
            this.dateViewWrapper = collapseView(i18n['search.dates'], this.dateView);
            this.relatedConceptsViewWrapper = collapseView(i18n['search.relatedConcepts'], this.relatedConceptsView);
        },

        render: function() {
            this.$el.html(this.template);

            this.filterDisplayView.setElement(this.$('.filter-display-container')).render();
            this.savedSearchControlView.setElement(this.$('.saved-search-controls-container')).render();
            this.indexesViewWrapper.setElement(this.$('.indexes-container')).render();
            this.parametricView.setElement(this.$('.parametric-container')).render();
            this.dateViewWrapper.setElement(this.$('.date-container')).render();
            this.spellCheckView.setElement(this.$('.spellcheck-container')).render();
            this.savedSearchesView.setElement(this.$('.saved-searches-container')).render();

            this.relatedConceptsViewWrapper.render();

            this.resultsNumberView.setElement(this.$('.results-number-container')).render();

            this.$('.related-concepts-container').append(this.relatedConceptsViewWrapper.$el);

            this.resultsViewContainer.setElement(this.$('.results-view-container')).render();

            this.$('.container-toggle').on('click', this.containerToggle);

        },

        containerToggle: function(event) {
            var $containerToggle = $(event.currentTarget);
            var $sideContainer = $containerToggle.closest('.side-container');
            var hide = !$sideContainer.hasClass('small-container');

            $sideContainer.find('.side-panel-content').toggleClass('hide', hide);
            $sideContainer.toggleClass('small-container', hide);
            $containerToggle.toggleClass('fa-rotate-180', hide);
        }
    });

});
