define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/model/dates-filter-model',
    'find/app/model/indexes-collection',
    'find/app/model/entity-collection',
    'find/app/model/search-filters-collection',
    'find/app/page/parametric/parametric-view',
    'find/app/page/filter-display/filter-display-view',
    'find/app/page/date/dates-filter-view',
    'find/app/page/results/results-view',
    'find/app/page/related-concepts/related-concepts-view',
    'find/app/page/sort/sort-view',
    'find/app/page/indexes/indexes-view',
    'find/app/util/collapsible',
    'parametric-refinement/selected-values-collection',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/service-view.html'
], function(Backbone, $, _, DatesFilterModel, IndexesCollection, EntityCollection, SearchFiltersCollection,
            ParametricView, FilterDisplayView, DateView, ResultsView, RelatedConceptsView, SortView,
            IndexesView, Collapsible, SelectedParametricValuesCollection, i18n, template) {

    var collapseView = function(titleKey, view) {
        return new Collapsible({
            view: view,
            collapsed: false,
            title: i18n[titleKey]
        });
    };

    return Backbone.View.extend({
        template: _.template(template)({i18n: i18n}),

        initialize: function(options) {
            this.queryModel = options.queryModel;

            this.datesFilterModel = new DatesFilterModel({}, {queryModel: this.queryModel});

            this.indexesCollection = new IndexesCollection();
            this.entityCollection = new EntityCollection();
            this.selectedParametricValues = new SelectedParametricValuesCollection();
            this.selectedIndexesCollection = new IndexesCollection();

            this.filtersCollection = new SearchFiltersCollection([], {
                queryModel: this.queryModel,
                datesFilterModel: this.datesFilterModel,
                indexesCollection: this.indexesCollection,
                selectedIndexesCollection: this.selectedIndexesCollection,
                selectedParametricValues: this.selectedParametricValues
            });

            this.indexesCollection.fetch();

            var fetchEntities = _.bind(function() {
                this.entityCollection.fetch({
                    data: {
                        text: this.queryModel.get('queryText'),
                        index: this.queryModel.get('indexes'),
                        field_text: this.queryModel.get('fieldText')
                    }
                });
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

            this.resultsView = new ResultsView({
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
            this.indexesView = new IndexesView({
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
                queryModel: this.queryModel
            });

            this.sortView = new SortView({
                queryModel: this.queryModel
            });

            // Collapse wrappers
            this.indexesViewWrapper = collapseView('search.indexes', this.indexesView);
            this.dateViewWrapper = collapseView('search.dates', this.dateView);
            this.relatedConceptsViewWrapper = collapseView('search.relatedConcepts', this.relatedConceptsView);
        },

        render: function() {
            this.$el.html(this.template);

            this.filterDisplayView.setElement(this.$('.filter-display-container')).render();
            this.indexesViewWrapper.setElement(this.$('.indexes-container')).render();
            this.parametricView.setElement(this.$('.parametric-container')).render();
            this.dateViewWrapper.setElement(this.$('.date-container')).render();

            this.relatedConceptsViewWrapper.render();

            this.sortView.setElement(this.$('.sort-container')).render();

            this.$('.related-concepts-container').append(this.relatedConceptsViewWrapper.$el);

            this.resultsView.setElement(this.$('.results-container')).render();
        }
    });

});
