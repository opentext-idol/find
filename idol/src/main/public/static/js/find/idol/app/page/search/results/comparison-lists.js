define([
    'backbone',
    'find/idol/app/model/comparison/comparison-documents-collection',
    'find/idol/app/page/search/results/idol-results-view',
    'find/app/page/search/results/state-token-strategy',
    'text!find/idol/templates/comparison/comparison-list-container.html',
    'find/app/util/search-data-util',
    'i18n!find/nls/bundle',
    'i18n!find/idol/nls/comparisons'
], function(Backbone, ComparisonDocumentsCollection, ResultsView,  stateTokenStrategy,
            comparisonListContainer, searchDataUtil, i18n, comparisonsI18n) {

    return Backbone.View.extend({
        className: 'service-view-container container-fluid',
        comparisonListContainer: _.template(comparisonListContainer, {variable: 'data'}),


        initialize: function(options) {
            this.searchModels = options.searchModels;
            this.escapeCallback = options.escapeCallback;

            this.resultsLists = {
                both: this.constructComparisonResultsView(this.model.get('bothText'), this.model.get('inBoth'), [this.searchModels.first, this.searchModels.second]),
                first: this.constructComparisonResultsView(this.model.get('firstText'), this.model.get('onlyInFirst'), [this.searchModels.first]),
                second: this.constructComparisonResultsView(this.model.get('secondText'), this.model.get('onlyInSecond'), [this.searchModels.second])
            };
        },

        render: function() {
            this.$el
                .append(this.comparisonListContainer({
                    position: 'left',
                    title: comparisonsI18n['list.title.first'](this.searchModels.first.get('title')),
                    identifier: 'first'
                }))
                .append(this.comparisonListContainer({
                    position: 'middle',
                    title: comparisonsI18n['list.title.both'],
                    identifier: 'both'
                }))
                .append(this.comparisonListContainer({
                    position: 'right',
                    title: comparisonsI18n['list.title.second'](this.searchModels.second.get('title')),
                    identifier: 'second'
                }));

            this.$('.comparison-results-view-both').append(this.resultsLists.both.$el);
            this.$('.comparison-results-view-first').append(this.resultsLists.first.$el);
            this.$('.comparison-results-view-second').append(this.resultsLists.second.$el);

            _.invoke(this.resultsLists, 'render');
        },

        constructComparisonResultsView: function(queryText, stateTokens, searchModels) {
            var collection = new ComparisonDocumentsCollection();

            var indexes = _.chain(searchModels).reduce(function(indexes, model) {
                return indexes.concat(searchDataUtil.buildIndexes(model.get('indexes')));
            }, []).uniq().value();

            var queryModel = new Backbone.Model(_.extend({
                queryText: queryText,
                indexes: indexes
            }, stateTokens));

            return new ResultsView({
                // ToDo Add support for promotions with comparison view (part of FIND-30)
                // Can then remove hidePromotions param
                hidePromotions: true,
                queryModel: queryModel,
                documentsCollection: collection,
                fetchStrategy: stateTokenStrategy
            });
        }
    });

});