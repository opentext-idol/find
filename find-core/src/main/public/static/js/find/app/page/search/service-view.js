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
    'find/app/page/search/results/results-view-container',
    'find/app/page/search/related-concepts/related-concepts-view',
    'find/app/page/search/sort-view',
    'find/app/page/search/results/results-number-view',
    'find/app/page/search/spellcheck-view',
    'find/app/page/search/saved-searches/saved-search-options',
    'find/app/util/collapsible',
    'parametric-refinement/selected-values-collection',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'text!find/templates/app/page/search/service-view.html'
], function(Backbone, $, _, DatesFilterModel, DocumentsCollection, IndexesCollection, EntityCollection, QueryModel, SearchFiltersCollection,
            ParametricView, FilterDisplayView, DateView, ResultsViewContainer, RelatedConceptsView, SortView, SpellCheckView, SavedSearchOptions,
            Collapsible, SelectedParametricValuesCollection, i18n, i18n_indexes, template) {

    'use strict';

    var html = _.template(template)({i18n: i18n});

    var collapseView = function (title, view) {
        return new Collapsible({
            view: view,
            collapsed: false,
            title: title
        });
    };

    return Backbone.View.extend({
        className: 'col-xs-12',

        // May be overridden
        SearchFiltersCollection: SearchFiltersCollection,

        // Abstract
        ResultsViewContainer: null,
        IndexesView: null,

        initialize: function(options) {
            this.searchModel = options.searchModel;
            this.queryTextModel = options.queryTextModel;

            this.queryModel = new QueryModel({
                queryText: this.model.get('queryText')
            });

            this.listenTo(this.searchModel, 'change:queryText', function(model, queryText) {
                if (model.get('selectedSearchCid') === this.model.cid) {
                    this.queryModel.set('queryText', queryText);
                }
            });

            this.datesFilterModel = new DatesFilterModel({}, {queryModel: this.queryModel});

            this.documentsCollection = new DocumentsCollection();
            this.indexesCollection = new IndexesCollection();
            this.entityCollection = new EntityCollection();

            // TODO: Display name?
            this.selectedParametricValues = new SelectedParametricValuesCollection(this.model.get('parametricValues'));

            // TODO: Support HOD domains
            // TODO: Check if the index still exists?
            this.selectedIndexesCollection = new IndexesCollection(_.map(this.model.get('indexes'), function(indexName) {
                return {name: indexName};
            }));

            this.filtersCollection = new this.SearchFiltersCollection([], {
                queryModel: this.queryModel,
                datesFilterModel: this.datesFilterModel,
                indexesCollection: this.indexesCollection,
                selectedIndexesCollection: this.selectedIndexesCollection,
                selectedParametricValues: this.selectedParametricValues
            });

            this.indexesCollection.fetch();

            var fetchEntities = _.bind(function() {
                if (this.queryModel.get('queryText') && this.queryModel.get('indexes').length !== 0) {
                    this.entityCollection.fetch({
                        data: _.extend(this.queryModel.pick(['queryText', 'indexes', 'fieldText']), {
                            minDate: this.queryModel.getIsoDate('minDate'),
                            maxDate: this.queryModel.getIsoDate('maxDate')
                        })
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

            this.savedSearchOptions = new SavedSearchOptions({
                model: this.model
            });

            this.resultsView = new this.ResultsViewContainer({
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
            this.indexesView = new this.IndexesView({
                queryModel: this.queryModel,
                indexesCollection: this.indexesCollection,
                selectedDatabasesCollection: this.selectedIndexesCollection
            });

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

            this.sortView = new SortView({
                queryModel: this.queryModel
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
            this.$el.html(html);

            this.filterDisplayView.setElement(this.$('.filter-display-container')).render();
            this.indexesViewWrapper.setElement(this.$('.indexes-container')).render();
            this.parametricView.setElement(this.$('.parametric-container')).render();
            this.dateViewWrapper.setElement(this.$('.date-container')).render();
            this.spellCheckView.setElement(this.$('.spellcheck-container')).render();
            this.savedSearchOptions.setElement(this.$('.saved-search-options')).render();

            this.relatedConceptsViewWrapper.render();

            this.sortView.setElement(this.$('.sort-container')).render();
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
