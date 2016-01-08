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
    'find/app/page/search/results/results-view',
    'find/app/page/search/related-concepts/related-concepts-view',
    'find/app/page/search/sort-view',
    'find/app/page/search/spellcheck-view',
    'find/app/page/search/filters/indexes/indexes-view',
    'find/app/util/collapsible',
    'parametric-refinement/selected-values-collection',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'text!find/templates/app/page/search/service-view.html'
], function(Backbone, $, _, DatesFilterModel, DocumentsCollection, IndexesCollection, EntityCollection, SearchFiltersCollection,
            ParametricView, FilterDisplayView, DateView, ResultsView, RelatedConceptsView, SortView, SpellCheckView,
            IndexesView, Collapsible, SelectedParametricValuesCollection, i18n, i18n_indexes, template) {
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
        constructResultsView: function (models) {
            return new ResultsView(models);
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;

            this.datesFilterModel = new DatesFilterModel({}, {queryModel: this.queryModel});

            this.documentsCollection = new DocumentsCollection();
            this.indexesCollection = new IndexesCollection();
            this.entityCollection = new EntityCollection();
            this.selectedParametricValues = new SelectedParametricValuesCollection();
            this.selectedIndexesCollection = new IndexesCollection();

            this.filtersCollection = this.constructSearchFiltersCollection(this.queryModel, this.datesFilterModel, this.indexesCollection, this.selectedIndexesCollection, this.selectedParametricValues);

            this.indexesCollection.fetch();

            var fetchEntities = _.bind(function() {
                if (this.queryModel.get('indexes').length !== 0) {
                    this.entityCollection.fetch({
                        data: {
                            text: this.queryModel.get('queryText'),
                            index: this.queryModel.get('indexes'),
                            field_text: this.queryModel.get('fieldText')
                        }
                    });
                }
            }, this);

            this.listenTo(this.queryModel, 'refresh', fetchEntities);

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

            this.resultsView = this.constructResultsView({
                documentsCollection: this.documentsCollection,
                entityCollection: this.entityCollection,
                indexesCollection: this.indexesCollection,
                queryModel: this.queryModel
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
                queryModel: this.queryModel
            });

            this.sortView = new SortView({
                queryModel: this.queryModel
            });

            this.spellCheckView = new SpellCheckView({
                documentsCollection: this.documentsCollection
            });

            // Collapse wrappers
            this.indexesViewWrapper = collapseView(i18n_indexes['search.indexes'], this.indexesView);
            this.dateViewWrapper = collapseView(i18n['search.dates'], this.dateView);
            this.relatedConceptsViewWrapper = collapseView(i18n['search.relatedConcepts'], this.relatedConceptsView);
        },

        render: function() {
            this.$el.html(this.template);

            this.filterDisplayView.setElement(this.$('.filter-display-container')).render();
            this.indexesViewWrapper.setElement(this.$('.indexes-container')).render();
            this.parametricView.setElement(this.$('.parametric-container')).render();
            this.dateViewWrapper.setElement(this.$('.date-container')).render();
            this.spellCheckView.setElement(this.$('.spellcheck-container')).render();

            this.relatedConceptsViewWrapper.render();

            this.sortView.setElement(this.$('.sort-container')).render();

            this.$('.related-concepts-container').append(this.relatedConceptsViewWrapper.$el);

            this.resultsView.setElement(this.$('.results-container')).render();

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
