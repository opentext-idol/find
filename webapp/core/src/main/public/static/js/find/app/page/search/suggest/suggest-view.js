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
const Backbone = require('backbone');
const vent = require('find/app/vent');
const suggestStrategy = require('find/app/page/search/results/suggest-strategy');
const SimilarDocumentsCollection = require('find/app/model/similar-documents-collection');
const i18n = require('find/nls/bundle');
const template = require('find/templates/app/page/search/suggest/suggest-view.html');

module.exports = Backbone.View.extend({
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

