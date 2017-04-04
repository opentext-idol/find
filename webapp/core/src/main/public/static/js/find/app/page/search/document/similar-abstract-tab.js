/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'i18n!find/nls/bundle',
    'find/app/vent',
    'text!find/templates/app/page/search/document/similar-documents-tab.html'
], function(_, $, Backbone, i18n, vent, template) {
    'use strict';

    return Backbone.View.extend({
        similarDocumentTemplate: _.template('<li data-cid="<%-cid%>" class="clickable"><h4><%-model.get("title")%></h4><p><%-model.get("summary").trim().substring(0, 200) + "\u2026"%></p></li>'),
        template: _.template(template),

        events: {
            'click [data-cid]': function(e) {
                vent.navigateToDetailRoute(
                    this.collection.get(
                        $(e.currentTarget).data('cid')
                    )
                );
            }
        },

        initialize: function(options) {
            this.indexesCollection = options.indexesCollection;
            this.collection = this.createCollection();
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.postRender();

            if(this.indexesCollection.length) {
                this.getSimilarDocuments();
            } else {
                this.listenTo(this.indexesCollection, 'sync', this.getSimilarDocuments);
            }
        },

        getSimilarDocuments: function() {
            this.$('.loading-spinner').removeClass('hide');

            let html;

            this.collection
                .fetch({
                    data: this.fetchData()
                })
                .done(_.bind(function() {
                        const filteredModels = this.collection.filter(function(model) {
                            return model.get('reference') !== this.model.get('reference');
                        }, this);

                        html = _.isEmpty(filteredModels)
                            ? i18n['search.similarDocuments.none']
                            : _.map(filteredModels, function(model) {
                                return this.similarDocumentTemplate({
                                    model: model,
                                    cid: model.cid
                                });
                            }, this).join('');
                    }, this)
                )
                .fail(_.bind(function(xhr) {
                    if(xhr.status !== 0) {
                        html = i18n['search.similarDocuments.error'];
                    }
                }, this))
                .always(_.bind(function(results, success) {
                    if(success !== 'abort') {
                        this.$('.loading-spinner').addClass('hide');
                    }

                    this.$('ul').html(html || '');
                }, this));
        },

        createCollection: null,
        fetchData: null,
        postRender: _.noop
    });
});
