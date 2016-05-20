/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'find/app/vent',
    'find/app/page/search/results/suggest-strategy',
    'find/app/model/similar-documents-collection',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/suggest/suggest-view.html'
], function(Backbone, _, vent, suggestStrategy, SimilarDocumentsCollection, i18n, template) {

    'use strict';

    return Backbone.View.extend({
        className: 'flex-container',
        template: _.template(template),

        // Abstract
        ResultsView: null,
        getIndexes: $.noop,

        events: {
            'click .suggest-view-button': function() {
                vent.navigate(this.backUrl);
            },
            'click .suggest-view-title': function() {
                vent.navigateToDetailRoute(this.documentModel);
            }
        },

        initialize: function(options) {
            this.backUrl = options.backUrl;
            this.documentModel = options.documentModel;

            this.resultsView = new this.ResultsView({
                fetchStrategy: suggestStrategy,
                documentsCollection: new SimilarDocumentsCollection(),
                queryModel: new Backbone.Model({
                    reference: this.documentModel.get('reference'),
                    indexes: this.getIndexes(options.indexesCollection, this.documentModel)
                })
            });

            this.listenTo(options.indexesCollection, 'update reset', function() {
                this.resultsView.queryModel.set('indexes', this.getIndexes(options.indexesCollection, this.documentModel));
            });
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                title: this.documentModel.get('title')
            }));

            this.$('.suggest-view-results').append(this.resultsView.$el);
            this.resultsView.render();
        }
    });

});
