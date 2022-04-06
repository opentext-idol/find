/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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
    'backbone',
    'underscore',
    'find/app/vent',
    'i18n!find/nls/bundle',
    'find/app/model/document-model',
    'find/app/configuration',
    'find/app/util/generate-error-support-message',
    'text!find/templates/app/page/loading-spinner.html',
    'text!find/templates/app/page/search/document-content-view.html'
], function(Backbone, _, vent, i18n, DocumentModel, configuration, generateErrorMessage, loadingTemplate, template) {
    'use strict';

    const ViewState = {
        LOADING: 'LOADING',
        ERROR: 'ERROR',
        OK: 'OK'
    };

    /**
     * Fetches a document model for the given reference and index, then renders the given ContentView.
     */
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
            this.ContentView = options.ContentView;
            this.contentViewOptions = options.contentViewOptions || {};

            this.viewModel = new Backbone.Model({state: ViewState.LOADING});
            this.documentModel = new DocumentModel();

            this.documentModel.fetch({
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

            this.$content = this.$('.document-content-content');
            this.$loading = this.$('.document-content-loading');
            this.$error = this.$('.document-content-error');
            this.$errorMessage = this.$('.document-content-error-message');

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

                    this.contentView = new this.ContentView(_.extend({
                        documentModel: this.documentModel
                    }, this.contentViewOptions));

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
