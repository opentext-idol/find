define([
    'backbone',
    'find/idol/app/model/comparison/comparison-documents-collection',
    'find/idol/app/page/search/results/idol-results-view',
    'find/idol/app/page/search/results/comparison-lists',
    'find/idol/app/page/search/results/comparison-map',
    'find/idol/app/page/search/results/comparison-topic-map',
    'find/app/page/search/results/state-token-strategy',
    'find/app/util/results-view-container',
    'find/app/util/results-view-selection',
    'text!find/idol/templates/comparison/comparison-view.html',
    'text!find/idol/templates/comparison/comparison-list-container.html',
    'find/app/util/search-data-util',
    'i18n!find/nls/bundle'
], function(Backbone, ComparisonDocumentsCollection, ResultsView, ResultsLists, ComparisonMap, ComparisonTopicMap,  stateTokenStrategy, ResultsViewContainer, ResultsViewSelection,
            template, comparisonListContainer, searchDataUtil, i18n) {

    return Backbone.View.extend({
        className: 'service-view-container',
        template: _.template(template),

        events: {
            'click .comparison-view-back-button': function() {
                this.escapeCallback();
            }
        },

        initialize: function(options) {
            this.searchModels = options.searchModels;
            this.escapeCallback = options.escapeCallback;

            var resultsViews = [
                {
                    Constructor: ResultsLists,
                    id: 'list',
                    uniqueId: _.uniqueId('results-view-item-'),
                    constructorArguments: {
                        searchModels: options.searchModels,
                        escapeCallback: options.escapeCallback,
                        model: this.model
                    },
                    selector: {
                        displayNameKey: 'list',
                        icon: 'hp-list'
                    }
                },
                {
                    Constructor: ComparisonMap,
                    id: 'map',
                    uniqueId: _.uniqueId('results-view-item-'),
                    constructorArguments: {
                        searchModels: options.searchModels,
                        escapeCallback: options.escapeCallback,
                        model: this.model
                    },
                    selector: {
                        displayNameKey: 'map',
                        icon: 'hp-map-view'
                    }
                },
                {
                    Constructor: ComparisonTopicMap,
                    id: 'topic-map',
                    uniqueId: _.uniqueId('results-view-item-'),
                    constructorArguments: {
                        searchModels: options.searchModels,
                        escapeCallback: options.escapeCallback,
                        model: this.model
                    },
                    selector: {
                        displayNameKey: 'topic-map',
                        icon: 'hp-grid'
                    }
                }
            ];

            var resultsViewSelectionModel = new Backbone.Model({
                // ID of the currently selected tab
                selectedTab: resultsViews[0].id
            });

            if (resultsViews.length > 1) {
                this.resultsViewSelection = new ResultsViewSelection({
                    views: resultsViews,
                    model: resultsViewSelectionModel
                });
            }

            this.resultsViewContainer = new ResultsViewContainer({
                views: resultsViews,
                model: resultsViewSelectionModel
            });
        },

        render: function() {
            this.$el.html(this.template({i18n: i18n}));

            if (this.resultsViewSelection) {
                this.resultsViewSelection.setElement(this.$('.results-view-selection')).render();
            }

            this.resultsViewContainer.setElement(this.$('.results-view-container')).render();
        }
    });

});