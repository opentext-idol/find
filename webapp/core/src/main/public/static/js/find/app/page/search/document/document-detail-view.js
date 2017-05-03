/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'find/app/vent',
    'i18n!find/nls/bundle',
    'find/app/model/document-model',
    'find/app/configuration',
    'find/app/page/search/document/document-detail-content-view',
    'find/app/util/generate-error-support-message',
    'text!find/templates/app/page/loading-spinner.html',
    'text!find/templates/app/page/search/document/document-detail-view.html'
], function(Backbone, _, vent, i18n, DocumentModel, configuration, ContentView, generateErrorMessage, loadingTemplate, template) {
    'use strict';

    const ViewState = {
        LOADING: 'LOADING',
        ERROR: 'ERROR',
        OK: 'OK'
    };

    return Backbone.View.extend({
        template: _.template(template),

        loadingHtml: _.template(loadingTemplate)({
            large: true,
            i18n: i18n
        }),

        className: 'service-view-flex-container',

        events: {
            'click .detail-view-back-button': function() {
                vent.navigate(this.backUrl);
            }
        },

        initialize: function(options) {
            this.backUrl = options.backUrl;
            this.indexesCollection = options.indexesCollection;
            this.mmapTab = options.mmapTab;

            this.viewModel = new Backbone.Model({state: ViewState.LOADING});
            this.model = new DocumentModel();

            this.model.fetch({
                    data: {
                        reference: options.reference,
                        database: options.database
                    }
                })
                .done(function() {
                    this.viewModel.set('state', ViewState.OK);
                }.bind(this))
                .fail(function(xhr) {
                    this.viewModel.set({
                        error: xhr.responseJSON,
                        state: ViewState.ERROR
                    });
                }.bind(this));

            this.listenTo(this.viewModel, 'change', this.updateState);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                loadingHtml: this.loadingHtml,
                relatedConcepts: configuration().enableRelatedConcepts
            }));

            this.$content = this.$('.document-detail-content');
            this.$loading = this.$('.document-detail-loading');
            this.$error = this.$('.document-detail-error');
            this.$errorMessage = this.$('.document-detail-error-message');

            this.updateState();
        },

        updateState: function() {
            const state = this.viewModel.get('state');

            if (this.$loading) {
                this.$loading.toggleClass('hide', state !== ViewState.LOADING);
            }

            if (this.$error) {
                this.$error.toggleClass('hide', state !== ViewState.ERROR);

                if (state === ViewState.ERROR) {
                    const error = this.viewModel.get('error');

                    const errorHtml = generateErrorMessage({
                        errorHeader: i18n['error.message.default'],
                        messageToUser: i18n['search.document.detail.loadingError'],
                        errorLookup: error.backendErrorCode,
                        errorUUID: error.uuid,
                        errorDetails: error.message
                    });

                    this.$errorMessage.html(errorHtml);
                }
            }

            if (this.$content) {
                this.$content.toggleClass('hide', state !== ViewState.OK);

                if (state === ViewState.OK) {
                    this.removeContentView();

                    this.contentView = new ContentView({
                        indexesCollection: this.indexesCollection,
                        mmapTab: this.mmapTab,
                        model: this.model
                    });

                    this.$content.append(this.contentView.$el);
                    this.contentView.render();
                }
            }
        },

        removeContentView: function() {
            if (this.contentView) {
                this.contentView.remove();
                this.contentView = null;
            }
        },

        remove: function() {
            this.removeContentView();
            Backbone.View.prototype.remove.call(this);
        }
    });
});
