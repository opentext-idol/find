/*
 * Copyright 2016-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

const _ = require('underscore');
const $ = require('jquery');
const Backbone = require('backbone');
const ComparisonDocumentsCollection = require('find/idol/app/model/comparison/comparison-documents-collection');
const ResultsView = require('find/idol/app/page/search/results/idol-results-view');
const stateTokenStrategy = require('find/app/page/search/results/state-token-strategy');
const comparisonListContainer = require('find/idol/templates/comparison/comparison-list-container.html');
const searchDataUtil = require('find/app/util/search-data-util');
const i18n = require('find/nls/bundle');
const comparisonsI18n = require('find/idol/nls/comparisons');

module.exports = Backbone.View.extend({
        className: 'service-view-container container-fluid',
        comparisonListContainer: _.template(comparisonListContainer, {variable: 'data'}),

        initialize: function(options) {
            this.searchModels = options.searchModels;
            this.documentRenderer = options.documentRenderer;
            this.escapeCallback = options.escapeCallback;
            this.scrollModel = options.scrollModel;

            this.resultsLists = {
                both: this.constructComparisonResultsView(
                    this.model.get('bothText'),
                    this.model.get('inBoth'),
                    [this.searchModels.first, this.searchModels.second]
                ),
                first: this.constructComparisonResultsView(
                    this.model.get('firstText'),
                    this.model.get('onlyInFirst'),
                    [this.searchModels.first]
                ),
                second: this.constructComparisonResultsView(
                    this.model.get('secondText'),
                    this.model.get('onlyInSecond'),
                    [this.searchModels.second]
                )
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

            this.$('.comparison-results-view-first').append(this.resultsLists.first.$el);
            this.$('.comparison-results-view-both').append(this.resultsLists.both.$el);
            this.$('.comparison-results-view-second').append(this.resultsLists.second.$el);

            _.invoke(this.resultsLists, 'render');
        },

        constructComparisonResultsView: function(queryText, stateTokens, searchModels) {
            const collection = new ComparisonDocumentsCollection();

            const indexes = _.chain(searchModels).reduce(function(indexes, model) {
                return indexes.concat(searchDataUtil.buildIndexes(model.get('indexes')));
            }, []).uniq().value();

            const queryModel = new Backbone.Model(_.extend({
                queryText: queryText,
                indexes: indexes
            }, stateTokens));

            return new ResultsView({
                // ToDo Add support for promotions with comparison view (part of FIND-30)
                // Can then remove hidePromotions param
                hidePromotions: true,
                documentRenderer: this.documentRenderer,
                queryModel: queryModel,
                documentsCollection: collection,
                fetchStrategy: stateTokenStrategy,
                scrollModel: this.scrollModel
            });
        }
    });

