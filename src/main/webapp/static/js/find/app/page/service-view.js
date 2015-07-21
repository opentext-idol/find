define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/model/entity-collection',
    'find/app/model/search-filters-collection',
    'find/app/model/parametric-collection',
    'find/app/page/parametric/parametric-controller',
    'find/app/page/filter-display/filter-display-view',
    'find/app/page/date/dates-filter-view',
    'find/app/page/results/results-view',
    'find/app/page/related-concepts/related-concepts-view',
    'find/app/page/sort/sort-view',
    'find/app/page/indexes/indexes-view',
    'find/app/util/collapsible',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/service-view.html',
    'text!find/templates/app/util/filter-header.html'
], function(Backbone, $, _, EntityCollection, SearchFiltersCollection, ParametricCollection,
            ParametricController, FilterDisplayView, DateView, ResultsView, RelatedConceptsView, SortView,
            IndexesView, Collapsible, i18n, template, filterHeader) {

    var filterHeaderTemplate = _.template(filterHeader);

    var collapseView = function(title, collapseParameter, view, collapsed) {
        return new Collapsible({
            header: filterHeaderTemplate({
                title: i18n[title]
            }),
            name: collapseParameter,
            view: view,
            collapsed: collapsed
        });
    };

    return Backbone.View.extend({
        template: _.template(template),

        initialize: function(options) {
            this.queryModel = options.queryModel;

            this.entityCollection = new EntityCollection();
            this.filtersCollection = new SearchFiltersCollection([], {queryModel: this.queryModel});

            this.listenTo(this.filtersCollection, 'remove', function(model) {
                var type = model.get('type');

                if (type === SearchFiltersCollection.FilterTypes.indexes) {
                    this.indexesView.selectAll();
                } else if (type === SearchFiltersCollection.FilterTypes.PARAMETRIC) {
                    this.queryModel.set('fieldText', null);
                }
            });

            this.listenTo(this.queryModel, 'change', function() {
                this.entityCollection.fetch({
                    data: {
                        text: this.queryModel.get('queryText'),
                        index: this.queryModel.get('indexes'),
                        field_text: this.queryModel.getFieldTextString()
                    }
                });
            });

            this.resultsView = new ResultsView({
                queryModel: this.queryModel,
                entityCollection: this.entityCollection
            });

            // Left Collapsed Views
            this.filterDisplayView = new FilterDisplayView({
                collection: this.filtersCollection
            });

            this.indexesView = new IndexesView({
                queryModel: this.queryModel
            });

            this.parametricController = new ParametricController({
                queryModel: this.queryModel
            });

            this.dateView = new DateView({
                queryModel: this.queryModel
            });

            //Right Collapsed View
            this.relatedConceptsView = new RelatedConceptsView({
                queryModel: this.queryModel,
                entityCollection: this.entityCollection
            });

            this.sortView = new SortView({
                queryModel: this.queryModel
            });

            // Collapse wrappers
            this.indexesViewWrapper = collapseView('search.indexes', 'indexes-filter', this.indexesView, false);
            this.parametricViewWrapper = collapseView('parametric.title', 'parametric-filter', this.parametricController.view, false);
            this.dateViewWrapper = collapseView('search.dates', 'dates-filter', this.dateView, false);
            this.relatedConceptsViewWrapper = collapseView('search.relatedConcepts', 'related-concepts', this.relatedConceptsView, false);
        },

        render: function() {
            this.$el.html(this.template);

            this.filterDisplayView.setElement(this.$('.filter-display-container')).render();
            this.indexesViewWrapper.setElement(this.$('.indexes-container')).render();
            this.parametricViewWrapper.setElement(this.$('.parametric-container')).render();
            this.dateViewWrapper.setElement(this.$('.date-container')).render();

            this.relatedConceptsViewWrapper.render();

            this.sortView.setElement(this.$('.sort-container')).render();

            this.$('.related-concepts-container').append(this.relatedConceptsViewWrapper.$el);

            this.resultsView.setElement(this.$('.results-container')).render();
        }
    })
});