define([
    'backbone',
    'find/app/model/comparisons/comparison-documents-collection',
    'find/app/page/search/results/state-token-strategy',
    'text!find/templates/app/page/search/saved-searches/comparison/comparison-view.html',
    'text!find/templates/app/page/search/saved-searches/comparison/comparison-list-container.html',
    'find/app/util/search-data-util',
    'i18n!find/nls/bundle'
], function (Backbone, ComparisonDocumentsCollection, stateTokenStrategy, template, comparisonListContainer, searchDataUtil, i18n) {

    return Backbone.View.extend({
        template: _.template(template),
        comparisonListContainer: _.template(comparisonListContainer, {variable: 'data'}),

        ResultsView: null,

        events: {
            'click .comparison-view-back-button': function () {
                this.escapeCallback();
            }
        },

        initialize: function (options) {
            this.searchModels = options.searchModels;
            this.escapeCallback = options.escapeCallback;

            this.resultsLists = {
                both: this.constructComparisonResultsView(this.model.get('bothText'), this.model.get('inBoth'), [this.searchModels.first, this.searchModels.second]),
                first: this.constructComparisonResultsView(this.model.get('firstText'), this.model.get('onlyInFirst'), [this.searchModels.first]),
                second: this.constructComparisonResultsView(this.model.get('secondText'), this.model.get('onlyInSecond'), [this.searchModels.second])
            };
        },

        render: function () {
            this.$el.html(this.template({i18n: i18n}));

            this.$comparisonView = this.$('.comparison-view');

            this.$comparisonView.append(this.comparisonListContainer(
                {
                    position: 'left',
                    title: i18n['comparison.list.title.first'](this.searchModels.first.get('title')),
                    identifier: 'first'
                }
            ));

            this.$comparisonView.append(this.comparisonListContainer(
                {
                    position: 'middle',
                    title: i18n['comparison.list.title.both'],
                    identifier: 'both'
                }
            ));

            this.$comparisonView.append(this.comparisonListContainer(
                {
                    position: 'right',
                    title: i18n['comparison.list.title.second'](this.searchModels.second.get('title')),
                    identifier: 'second'
                }
            ));

            this.$('.comparison-results-view-both').append(this.resultsLists.both.$el);
            this.$('.comparison-results-view-first').append(this.resultsLists.first.$el);
            this.$('.comparison-results-view-second').append(this.resultsLists.second.$el);

            _.invoke(this.resultsLists, 'render');
        },

        constructComparisonResultsView: function (queryText, stateTokens, searchModels) {
            var collection = new ComparisonDocumentsCollection();

            var indexes = _.chain(searchModels).reduce(function(indexes, model) {
                return indexes.concat(searchDataUtil.buildIndexes(model.get('indexes')));
            }, []).uniq().value();

            var queryModel = new Backbone.Model(_.extend({
                queryText: queryText,
                indexes: indexes
            }, stateTokens));

            return new this.ResultsView({
                queryModel: queryModel,
                documentsCollection: collection,
                fetchStrategy: stateTokenStrategy
            });
        }
    });

});