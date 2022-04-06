/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'underscore',
    'backbone',
    'i18n!find/nls/bundle',
    'i18n!find/idol/nls/comparisons',
    'find/app/page/search/results/state-token-strategy',
    'find/app/util/search-data-util',
    'find/app/model/entity-collection',
    'find/app/page/search/results/entity-topic-map-view',
    'find/app/util/results-view-container',
    'find/app/util/results-view-selection',
    'find/app/configuration',
    'text!find/idol/templates/comparison/topic-map-comparison-view.html'
], function(_, Backbone, i18n, comparisonsI18n, stateTokenStrategy, searchDataUtil, EntityCollection,
            TopicMapView, ResultsViewContainer, ResultsViewSelection, configuration, html) {
    'use strict';

    return Backbone.View.extend({
        className: 'service-view-container',

        initialize: function(options) {
            this.searchModels = options.searchModels;

            const bothQueryModel = this.createQueryModel(this.model.get('bothText'), this.model.get('inBoth'), [this.searchModels.first, this.searchModels.second]);
            const firstQueryModel = this.createQueryModel(this.model.get('firstText'), this.model.get('onlyInFirst'), [this.searchModels.first]);
            const secondQueryModel = this.createQueryModel(this.model.get('secondText'), this.model.get('onlyInSecond'), [this.searchModels.second]);

            const commonContructorArguments = {
                clickHandler: _.noop,
                type: 'STATE_TOKEN',
                configuration: configuration()
            };

            const resultsViews = [
                {
                    Constructor: TopicMapView,
                    id: 'first',
                    uniqueId: _.uniqueId('results-view-item-'),
                    constructorArguments: _.extend({queryModel: firstQueryModel},
                        commonContructorArguments),
                    selector: {
                        displayName: comparisonsI18n['list.title.first'](this.searchModels.first.get('title')),
                        icon: 'hp-divide-in-right'
                    }
                },
                {
                    Constructor: TopicMapView,
                    id: 'both',
                    uniqueId: _.uniqueId('results-view-item-'),
                    constructorArguments: _.extend({queryModel: bothQueryModel},
                        commonContructorArguments),
                    selector: {
                        displayName: comparisonsI18n['list.title.both'],
                        icon: 'hp-divide-in-center'
                    }
                },
                {
                    Constructor: TopicMapView,
                    id: 'second',
                    uniqueId: _.uniqueId('results-view-item-'),
                    constructorArguments: _.extend({queryModel: secondQueryModel},
                        commonContructorArguments),
                    selector: {
                        displayName: comparisonsI18n['list.title.second'](this.searchModels.second.get('title')),
                        icon: 'hp-divide-in-right hp-flip-horizontal'
                    }
                }
            ];

            // Initially, select the left-most tab
            const initialTabId = resultsViews[0].id;

            const resultsViewSelectionModel = new Backbone.Model({
                // ID of the currently selected tab
                selectedTab: initialTabId
            });

            this.resultsViewSelection = new ResultsViewSelection({
                views: resultsViews,
                model: resultsViewSelectionModel
            });

            this.resultsViewContainer = new ResultsViewContainer({
                views: resultsViews,
                model: resultsViewSelectionModel
            });
        },

        render: function() {
            this.$el.html(html);

            this.resultsViewSelection.setElement(this.$('.topic-map-comparison-selection')).render();

            this.resultsViewContainer.setElement(this.$('.topic-map-comparison-container')).render();
        },

        createQueryModel: function(queryText, stateTokens, searchModels) {
            return new Backbone.Model(_.extend({
                queryText: queryText,
                indexes: _.chain(searchModels)
                    .map(function(model) {
                        return searchDataUtil.buildIndexes(model.get('indexes'));
                    })
                    .flatten()
                    .uniq()
                    .value()
            }, stateTokens));
        }
    })
});
