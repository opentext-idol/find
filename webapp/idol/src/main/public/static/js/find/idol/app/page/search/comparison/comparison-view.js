/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone',
    'find/idol/app/model/comparison/comparison-documents-collection',
    'find/idol/app/page/search/results/comparison-lists',
    'find/idol/app/page/search/results/comparison-map',
    'find/idol/app/page/search/results/comparison-topic-map',
    'find/app/page/search/results/state-token-strategy',
    'find/app/util/results-view-container',
    'find/app/util/results-view-selection',
    'find/app/configuration',
    'text!find/idol/templates/comparison/comparison-view.html',
    'i18n!find/nls/bundle'
], function(_, Backbone, ComparisonDocumentsCollection, ResultsLists, ComparisonMap,
            ComparisonTopicMap, stateTokenStrategy, ResultsViewContainer, ResultsViewSelection,
            configuration, template, i18n) {
    'use strict';

    const html = _.template(template)({i18n: i18n});

    return Backbone.View.extend({
        className: 'service-view-container',

        events: {
            'click .comparison-view-back-button': function() {
                this.escapeCallback();
            }
        },

        initialize: function(options) {
            this.searchModels = options.searchModels;
            this.escapeCallback = options.escapeCallback;

            const resultsViews = _.where([
                {
                    Constructor: ComparisonTopicMap,
                    id: 'topic-map',
                    uniqueId: _.uniqueId('results-view-item-'),
                    shown: true,
                    constructorArguments: {
                        searchModels: options.searchModels,
                        escapeCallback: options.escapeCallback,
                        model: this.model
                    },
                    selector: {
                        displayNameKey: 'topic-map',
                        icon: 'hp-grid'
                    }
                },
                {
                    Constructor: ResultsLists,
                    id: 'list',
                    uniqueId: _.uniqueId('results-view-item-'),
                    shown: true,
                    constructorArguments: {
                        documentRenderer: options.documentRenderer,
                        searchModels: options.searchModels,
                        escapeCallback: options.escapeCallback,
                        model: this.model,
                        scrollModel: options.scrollModel
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
                    shown: configuration().map.enabled,
                    constructorArguments: {
                        searchModels: options.searchModels,
                        escapeCallback: options.escapeCallback,
                        model: this.model
                    },
                    selector: {
                        displayNameKey: 'map',
                        icon: 'hp-map-view'
                    }
                }
            ], {shown: true});

            const resultsViewSelectionModel = new Backbone.Model({
                // ID of the currently selected tab
                selectedTab: resultsViews[0].id
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

            this.resultsViewSelection.setElement(this.$('.results-view-selection')).render();
            this.resultsViewContainer.setElement(this.$('.results-view-container')).render();
        },

        remove: function() {
            this.resultsViewSelection.remove();
            this.resultsViewContainer.remove();
            Backbone.View.prototype.remove.call(this);
        }
    });
});
