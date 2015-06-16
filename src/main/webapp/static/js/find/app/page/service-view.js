define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/model/entity-collection',
    'find/app/model/parametric-collection',
    'find/app/page/parametric/parametric-controller',
    'find/app/page/date/dates-filter-view',
    'find/app/page/results/results-view',
    'find/app/page/related-concepts/related-concepts-view',
    'find/app/util/collapsible',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/service-view.html',
    'text!find/templates/app/util/filter-header.html'
], function(Backbone, $, _, EntityCollection, ParametricCollection,
            ParametricController, DateView, ResultsView, RelatedConceptsView, Collapsible, i18n, template, filterHeader) {

    var filterHeaderTemplate = _.template(filterHeader);

    var collapseView = function(title, collapseParameter, view) {
        return new Collapsible({
            header: filterHeaderTemplate({
                title: i18n[title]
            }),
            name: collapseParameter,
            view: view
        });
    };

    return Backbone.View.extend({
        template: _.template(template),

        initialize: function(options) {
            this.queryModel = options.queryModel;

            this.entityCollection = new EntityCollection();

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
            this.parametricController = new ParametricController({
                queryModel: this.queryModel
            });

            this.dateView = new DateView({
                queryModel: this.queryModel
            });

            this.relatedConceptsView = new RelatedConceptsView({
                queryModel: this.queryModel,
                entityCollection: this.entityCollection
            });

            // Collapse wrappers

            this.parametricViewWrapper = collapseView('parametric.title', 'parametric-filter', this.parametricController.view);
            this.dateViewWrapper = collapseView('search.dates', 'dates-filter', this.dateView);
            this.relatedConceptsViewWrapper = collapseView('search.relatedConcepts', 'related-concepts', this.relatedConceptsView);
        },

        render: function() {
            this.$el.html(this.template);

            this.parametricViewWrapper.setElement(this.$('.parametric-container')).render();
            this.dateViewWrapper.setElement(this.$('.date-container')).render();

            this.relatedConceptsViewWrapper.render();

            this.$('.related-concepts-container').append(this.relatedConceptsViewWrapper.$el);

            this.resultsView.setElement(this.$('.results-container')).render();
        }
    })
});