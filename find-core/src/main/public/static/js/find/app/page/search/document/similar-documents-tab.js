/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'i18n!find/nls/bundle',
    'find/app/model/similar-documents-collection',
    'text!find/templates/app/page/search/document/similar-documents-tab.html'
], function(Backbone, _, i18n, SimilarDocumentsCollection, template) {
    'use strict';

    return Backbone.View.extend({
        similarDocumentTemplate: _.template('<li><h4><%-model.get("title")%></h4><p><%-model.get("summary").trim().substring(0, 200) + "..."%></p></li>'),
        template: _.template(template),

        initialize: function(options) {
            this.indexesCollection = options.indexesCollection;

            if(this.indexesCollection.length) {
                this.getSimilarDocuments();
            } else {
                this.listenTo(this.indexesCollection, 'sync', this.getSimilarDocuments);
            }
        },

        render: function() {
            this.$el.html(this.template({
                i18n:i18n
            }));
        },

        getSimilarDocuments: function() {
            this.$('.loading-spinner').removeClass('hide');

            var collection = new SimilarDocumentsCollection([], {
                indexes: this.indexesCollection.pluck('id'),
                reference: this.model.get('reference')
            });

            collection.fetch({
                error: _.bind(function() {
                    this.$el.html(i18n['search.similarDocuments.error']);
                }, this),
                success: _.bind(function() {
                    if (collection.isEmpty()) {
                        this.$el.html(i18n['search.similarDocuments.none']);
                    } else {
                        _.each(collection.models, function(model) {
                            this.$('ul').append(this.similarDocumentTemplate({model:model}));
                        }, this);
                    }
                }, this)
            }).always(_.bind(function() {
                this.$('.loading-spinner').addClass('hide');
            }, this));
        }

    });
});
