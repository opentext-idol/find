/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'i18n!find/nls/bundle',
    'find/app/model/similar-documents-collection',
    'find/app/vent',
    'text!find/templates/app/page/search/document/similar-documents-tab.html'
], function(Backbone, _, i18n, SimilarDocumentsCollection, vent, template) {
    'use strict';

    return Backbone.View.extend({
        similarDocumentTemplate: _.template('<li data-cid="<%-cid%>" class="clickable"><h4><%-model.get("title")%></h4><p><%-model.get("summary").trim().substring(0, 200) + "..."%></p></li>'),
        template: _.template(template),

        events: {
            'click [data-cid]': function(e) {
                var cid = $(e.currentTarget).data('cid');
                var model = this.collection.get(cid);

                vent.navigateToDetailRoute(model);
            }
        },

        initialize: function(options) {
            this.indexesCollection = options.indexesCollection;
        },

        render: function() {
            this.$el.html(this.template({
                i18n:i18n
            }));

            if(this.indexesCollection.length) {
                this.getSimilarDocuments();
            } else {
                this.listenTo(this.indexesCollection, 'sync', this.getSimilarDocuments);
            }
        },

        getSimilarDocuments: function() {
            this.$('.loading-spinner').removeClass('hide');

            this.collection = new SimilarDocumentsCollection([], {
                indexes: this.indexesCollection.pluck('id'),
                reference: this.model.get('reference')
            });

            this.collection.fetch({
                error: _.bind(function() {
                    this.$el.html(i18n['search.similarDocuments.error']);
                }, this),
                success: _.bind(function() {
                    if (this.collection.isEmpty()) {
                        this.$el.html(i18n['search.similarDocuments.none']);
                    } else {
                        _.each(this.collection.models, function(model) {
                            this.$('ul').append(this.similarDocumentTemplate({
                                model: model,
                                cid: model.cid
                            }));
                        }, this);
                    }
                }, this)
            }).always(_.bind(function() {
                this.$('.loading-spinner').addClass('hide');
            }, this));
        }

    });
});
