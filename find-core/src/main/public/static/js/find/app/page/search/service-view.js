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
    'find/app/page/search/spellcheck-view',
    'find/app/util/collapsible',
    'find/app/util/model-any-changed-attribute-listener',
    'parametric-refinement/selected-values-collection',
    'find/app/page/search/saved-searches/saved-search-control-view',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'text!find/templates/app/page/search/service-view.html'
], function(Backbone, $, _, DatesFilterModel, DocumentsCollection, IndexesCollection, EntityCollection, QueryModel, SearchFiltersCollection,
            ParametricView, FilterDisplayView, DateView, ResultsViewContainer, RelatedConceptsView, SpellCheckView,
            Collapsible, addChangeListener, SelectedParametricValuesCollection, SavedSearchControlView, i18n, i18nIndexes, template) {

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

    function buildQueryModelIndexes(selectedIndexesCollection) {
        return selectedIndexesCollection.map(function(model) {
            return model.get('domain') ? encodeURIComponent(model.get('domain')) + ':' + encodeURIComponent(model.get('name')) : encodeURIComponent(model.get('name'));
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
        ResultsViewContainer: null,
        IndexesView: null,

        initialize: function(options) {
            this.indexesCollection = options.indexesCollection;
            this.queryTextModel = options.queryTextModel;
            this.savedSearchCollection = options.savedSearchCollection;
            this.savedSearchModel = options.savedSearchModel;

            this.documentsCollection = new DocumentsCollection();
            this.entityCollection = new EntityCollection();

            this.selectedParametricValues = new SelectedParametricValuesCollection(this.savedSearchModel.toSelectedParametricValues());

            var initialSelectedIndexes;
            var savedSelectedIndexes = this.savedSearchModel.toSelectedIndexes();

            if (savedSelectedIndexes.length === 0) {
                if (this.indexesCollection.isEmpty()) {
                    initialSelectedIndexes = [];

                    this.listenToOnce(this.indexesCollection, 'sync', function() {
                        this.selectedIndexesCollection.set(selectInitialIndexes(this.indexesCollection));
                    });
                } else {
                    initialSelectedIndexes = selectInitialIndexes(this.indexesCollection);
                }
            } else {
                initialSelectedIndexes = savedSelectedIndexes;
            }

            // TODO: Check if the index still exists?
            this.selectedIndexesCollection = new IndexesCollection(initialSelectedIndexes);

            this.queryModel = new QueryModel(_.extend({
                queryText: this.queryTextModel.makeQueryText(),
                indexes: buildQueryModelIndexes(this.selectedIndexesCollection),
                fieldText: this.selectedParametricValues.toFieldTextNode() || null
            }, this.savedSearchModel.toQueryModelAttributes()));

            this.listenTo(this.queryTextModel, 'change', function() {
                this.queryModel.set('queryText', this.queryTextModel.makeQueryText());
            });

            this.datesFilterModel = new DatesFilterModel({}, {queryModel: this.queryModel});

            this.filtersCollection = new this.SearchFiltersCollection([], {
                queryModel: this.queryModel,
                datesFilterModel: this.datesFilterModel,
                indexesCollection: this.indexesCollection,
                selectedIndexesCollection: this.selectedIndexesCollection,
                selectedParametricValues: this.selectedParametricValues
            });

            addChangeListener(this, this.queryModel, ['queryText', 'indexes', 'fieldText'], this.fetchEntities);

            this.listenTo(this.selectedIndexesCollection, 'update reset', _.debounce(_.bind(function() {
                this.queryModel.set('indexes', buildQueryModelIndexes(this.selectedIndexesCollection));
            }, this), 500));

            this.savedSearchControlView = new SavedSearchControlView({
                savedSearchModel: this.savedSearchModel,
                savedSearchCollection: this.savedSearchCollection,
                queryModel: this.queryModel,
                queryTextModel: this.queryTextModel,
                selectedIndexesCollection: this.selectedIndexesCollection,
                selectedParametricValues: this.selectedParametricValues
            });
            
            this.resultsViewContainer = new this.ResultsViewContainer({
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

            this.spellCheckView = new SpellCheckView({
                documentsCollection: this.documentsCollection,
                queryModel: this.queryModel
            });

            // Collapse wrappers
            this.indexesViewWrapper = collapseView(i18nIndexes['search.indexes'], this.indexesView);
            this.dateViewWrapper = collapseView(i18n['search.dates'], this.dateView);
            this.relatedConceptsViewWrapper = collapseView(i18n['search.relatedConcepts'], this.relatedConceptsView);
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

            this.resultsViewContainer.setElement(this.$('.results-view-container')).render();

            this.$('.container-toggle').on('click', this.containerToggle);

            this.fetchEntities();
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
        }
    });

});
