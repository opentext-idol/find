/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/model/dates-filter-model',
    'find/app/model/documents-collection',
    'find/app/model/indexes-collection',
    'find/app/model/entity-collection',
    'find/app/page/search/filters/parametric/parametric-view',
    'find/app/page/search/filter-display/filter-display-view',
    'find/app/page/search/filters/date/dates-filter-view',
    'find/app/page/search/related-concepts/related-concepts-view',
    'find/app/page/search/sort-view',
    'find/app/page/search/results/query-strategy',
    'find/app/page/search/results/results-number-view',
    'find/app/page/search/spellcheck-view',
    'find/app/util/collapsible',
    'find/app/util/database-name-resolver',
    'parametric-refinement/selected-values-collection',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'text!find/templates/app/page/search/query-service-view.html'
], function (Backbone, $, _, DatesFilterModel, DocumentsCollection, IndexesCollection, EntityCollection,
             ParametricView, FilterDisplayView, DateView, RelatedConceptsView, SortView, queryStrategy, ResultsNumberView, SpellCheckView,
             Collapsible, databaseNameResolver, SelectedParametricValuesCollection, i18n, i18n_indexes, template) {
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

        // will be overridden
        IndexesView: null,
        ResultsView: null,
        SearchFiltersCollection: null,

        initialize: function (options) {
            this.highlightToggle = new Backbone.Model({ highlightConcept : false});

            this.queryModel = options.queryModel;
            this.queryTextModel = options.queryTextModel;
            this.indexesCollection = options.indexesCollection;

            this.datesFilterModel = new DatesFilterModel({}, {queryModel: this.queryModel});

            this.documentsCollection = new DocumentsCollection();
            this.entityCollection = new EntityCollection();
            this.selectedParametricValues = new SelectedParametricValuesCollection();
            this.selectedIndexesCollection = new IndexesCollection();

            this.filtersCollection = new this.SearchFiltersCollection([], {
                queryModel: this.queryModel,
                datesFilterModel: this.datesFilterModel,
                indexesCollection: this.indexesCollection,
                selectedIndexesCollection: this.selectedIndexesCollection,
                selectedParametricValues: this.selectedParametricValues
            });

            var fetchEntities = _.bind(function () {
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

            this.listenTo(this.queryModel, 'change', function () {
                if (this.queryModel.hasAnyChangedAttributes(['queryText', 'indexes', 'fieldText', 'minDate', 'maxDate'])) {
                    fetchEntities();
                }
            });

            this.listenTo(this.selectedIndexesCollection, 'update reset', _.debounce(_.bind(function () {
                this.queryModel.set('indexes', this.selectedIndexesCollection.map(function (model) {
                    return databaseNameResolver.resolveDatabaseNameForModel(model);
                }, this));
            }, this), 500));

            this.resultsView = new this.ResultsView({
                documentsCollection: this.documentsCollection,
                entityCollection: this.entityCollection,
                indexesCollection: this.indexesCollection,
                queryModel: this.queryModel,
                queryTextModel: this.queryTextModel,
                queryStrategy: queryStrategy,
                highlightToggle: this.highlightToggle
            });

            // Left Views
            this.filterDisplayView = new FilterDisplayView({
                collection: this.filtersCollection,
                datesFilterModel: this.datesFilterModel
            });

            this.parametricView = new ParametricView({
                queryModel: this.queryModel,
                selectedParametricValues: this.selectedParametricValues
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
                queryTextModel: this.queryTextModel,
                highlightToggle: this.highlightToggle
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

        render: function () {
            this.$el.html(this.template);

            this.filterDisplayView.setElement(this.$('.filter-display-container')).render();
            this.indexesViewWrapper.setElement(this.$('.indexes-container')).render();
            this.parametricView.setElement(this.$('.parametric-container')).render();
            this.dateViewWrapper.setElement(this.$('.date-container')).render();
            this.spellCheckView.setElement(this.$('.spellcheck-container')).render();

            this.relatedConceptsViewWrapper.render();

            this.sortView.setElement(this.$('.sort-container')).render();
            this.resultsNumberView.setElement(this.$('.results-number-container')).render();

            this.$('.related-concepts-container').append(this.relatedConceptsViewWrapper.$el);

            this.resultsView.setElement(this.$('.results-container')).render();

            this.$('.container-toggle').on('click', this.containerToggle);

        },

        containerToggle: function (event) {
            var $containerToggle = $(event.currentTarget);
            var $sideContainer = $containerToggle.closest('.side-container');
            var hide = !$sideContainer.hasClass('small-container');

            $sideContainer.find('.side-panel-content').toggleClass('hide', hide);
            $sideContainer.toggleClass('small-container', hide);
            $containerToggle.toggleClass('fa-rotate-180', hide);
        }
    });

});
