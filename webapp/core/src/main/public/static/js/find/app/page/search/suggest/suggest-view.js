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
    'find/app/vent',
    'find/app/page/search/results/suggest-strategy',
    'find/app/model/similar-documents-collection',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/suggest/suggest-view.html'
], function(_, Backbone, vent, suggestStrategy, SimilarDocumentsCollection, i18n, template) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(template),

        // Abstract
        ResultsView: null,
        ResultsViewAugmentation: null,
        getIndexes: null,

        events: {
            'click .suggest-view-button': function() {
                vent.navigate(this.backUrl);
            },
            'click .suggest-view-title': function() {
                vent.navigateToDetailRoute(this.documentModel);
            }
        },

        initialize: function(options) {
            this.documentModel = options.documentModel;
            this.scrollModel = options.scrollModel;
            this.configuration = options.configuration;

            this.queryModel = new Backbone.Model({
                reference: this.documentModel.get('reference'),
                indexes: this.getIndexes(options.indexesCollection, this.documentModel)
            });

            const previewModeModel = new Backbone.Model({document: null, mode: 'summary'});

            this.resultsView = new this.ResultsView({
                fetchStrategy: suggestStrategy,
                documentsCollection: new SimilarDocumentsCollection(),
                documentRenderer: options.documentRenderer,
                queryModel: this.queryModel,
                scrollModel: this.scrollModel,
                previewModeModel: previewModeModel
            });

            this.resultsViewAugmentation = new this.ResultsViewAugmentation({
                documentRenderer: options.documentRenderer,
                indexesCollection: options.indexesCollection,
                queryModel: this.queryModel,
                resultsView: this.resultsView,
                scrollModel: this.scrollModel,
                previewModeModel: previewModeModel,
                mmapTab: options.mmapTab
            });

            this.listenTo(options.indexesCollection, 'update reset', function() {
                this.queryModel.set('indexes', this.getIndexes(options.indexesCollection, this.documentModel));
            });
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                title: this.documentModel.get('title'),
                relatedConcepts: this.configuration.enableRelatedConcepts
            }));

            this.resultsViewAugmentation.setElement(this.$('.suggest-view-results')).render();
        },

        remove: function() {
            this.resultsViewAugmentation.remove();
            Backbone.View.prototype.remove.call(this);
        }
    });
});
