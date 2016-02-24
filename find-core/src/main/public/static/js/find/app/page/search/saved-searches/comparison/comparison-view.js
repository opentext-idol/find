define([
    'backbone',
    'find/app/model/query-model',
    'find/app/page/search/results/results-view',
    'text!find/templates/app/page/search/saved-searches/comparison/comparison-view.html',
    'text!find/templates/app/page/search/saved-searches/comparison/comparison-list-container.html',
    'find/app/util/search-data-util',
    'i18n!find/nls/bundle'
], function (Backbone, QueryModel, ResultsView, template, comparisonListContainer, searchDataUtil, i18n) {

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
                both: this.constructComparisonResultsView(this.model.get('documentsInBoth'), [this.searchModels.first, this.searchModels.second]),
                first: this.constructComparisonResultsView(this.model.get('documentsOnlyInFirst'), [this.searchModels.first]),
                second: this.constructComparisonResultsView(this.model.get('documentsOnlyInSecond'), [this.searchModels.second])
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

        constructComparisonResultsView: function (collection, searchModels) {
            var indexes = _.chain(searchModels).reduce(function(indexes, model) {
                return indexes.concat(searchDataUtil.buildIndexes(model.get('indexes')));
            }, []).uniq().value();

            return new this.ResultsView({
                mode: ResultsView.Mode.STATE_TOKEN,
                isComparisonView: true,
                queryModel: new Backbone.Model({
                    indexes: indexes
                }),
                entityCollection: new Backbone.Collection,
                documentsCollection: collection
            });
        }
    });

});