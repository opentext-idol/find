/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/model/similar-documents-collection',
    'find/app/page/search/sort-view',
    'find/app/page/search/results/results-message-view',
    'find/app/page/search/results/results-number-view',
    'find/app/page/search/results/suggest-strategy',
    'text!find/templates/app/page/search/suggest-service-view.html'
], function(Backbone, $, _, DocumentsCollection, SortView, ResultsMessageView, ResultsNumberView, suggestStrategy, template) {
    "use strict";

    return Backbone.View.extend({
        template: _.template(template)(),

        // will be overridden
        ResultsView: null,

        initialize: function(options) {
            this.queryModel = options.queryModel;
            this.indexesCollection = options.indexesCollection;
            this.backUrl = options.backUrl;

            this.documentsCollection = new DocumentsCollection();


            this.resultsView = new this.ResultsView({
                documentsCollection: this.documentsCollection,
                indexesCollection: this.indexesCollection,
                queryModel: this.queryModel,
                queryStrategy: suggestStrategy
            });

            this.sortView = new SortView({
                queryModel: this.queryModel
            });

            this.resultsMessageView = new ResultsMessageView({
                queryModel: this.queryModel,
                backUrl: this.backUrl
            });

            this.resultsNumberView = new ResultsNumberView({
                documentsCollection: this.documentsCollection
            });
        },

        render: function() {
            this.$el.html(this.template);

            this.sortView.setElement(this.$('.sort-container')).render();
            this.resultsMessageView.setElement(this.$('.results-message-container')).render();
            this.resultsNumberView.setElement(this.$('.results-number-container')).render();

            this.resultsView.setElement(this.$('.results-container')).render();

            this.resultsView.refreshResults();
        }
    });

});
