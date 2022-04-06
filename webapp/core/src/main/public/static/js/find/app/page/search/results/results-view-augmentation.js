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
    'underscore',
    'jquery',
    'backbone',
    'find/app/page/search/document/preview-mode-fact-view',
    'find/app/page/search/document/preview-mode-user-view',
    'text!find/templates/app/page/search/results/results-view-augmentation.html'
], function(_, $, Backbone, PreviewModeFactView, PreviewModeUserView, viewHtml) {
    'use strict';

    // We always want a gap between the preview well and the container
    const PREVIEW_MARGIN_PIXELS = 10;

    return Backbone.View.extend({
        // abstract
        PreviewModeSummaryView: null,

        initialize: function(options) {
            this.resultsView = options.resultsView;
            this.queryModel = options.queryModel;
            this.scrollModel = options.scrollModel;

            // Tracks document currently being previewed in the "documents" attribute
            this.previewModeModel = options.previewModeModel;

            this.listenTo(this.previewModeModel, 'change', function(model) {
                this.removePreviewModeView();
                const mode = model.get('mode');

                if (mode === 'summary') {
                    this.previewModeView = new this.PreviewModeSummaryView({
                        model: model.get('document'),
                        documentRenderer: options.documentRenderer,
                        previewModeModel: this.previewModeModel,
                        queryText: this.queryModel.get('queryText'),
                        indexesCollection: options.indexesCollection,
                        mmapTab: options.mmapTab
                    });

                } else if (mode === 'fact') {
                    this.previewModeView = new PreviewModeFactView({
                        model: model.get('fact'),
                        factsView: model.get('factsView'),
                        documentRenderer: options.documentRenderer,
                        previewModeModel: this.previewModeModel
                    });

                } else if (mode === 'user') {
                    this.previewModeView = new PreviewModeUserView({
                        model: model.get('user'),
                        previewModeModel: this.previewModeModel
                    });

                } else {
                    this.previewModeView = null;
                }

                if (this.previewModeView !== null) {
                    this.$previewModeContainer.append(this.previewModeView.$el);
                    this.previewModeView.render();
                    this.scrollFollow();
                }
                this.togglePreviewMode(mode != null);
            });

            this.listenTo(this.scrollModel, 'change', this.scrollFollow);
        },

        render: function() {
            this.$el.html(viewHtml);

            this.$previewModeContainer = this.$('.preview-mode-container');

            this.resultsView.setElement(this.$('.main-results-content')).render();
            this.scrollFollow();
        },

        update: function () {
            if (this.resultsView.update) {
                this.resultsView.update();
            }
            this.togglePreviewMode(this.previewModeModel.get('mode') != null);
        },

        togglePreviewMode: function(previewMode) {
            this.trigger('rightSideContainerHideToggle', !previewMode);

            this.$('.preview-mode-wrapper').toggleClass('hide', !previewMode);

            //making main results container smaller or bigger
            this.$('.main-results-content-container').toggleClass('col-md-6', previewMode);
            this.$('.main-results-content-container').toggleClass('col-md-12', !previewMode);
        },

        remove: function() {
            this.resultsView.remove();
            this.removePreviewModeView();
            Backbone.View.prototype.remove.call(this);
        },

        removePreviewModeView: function() {
            if(this.previewModeView) {
                this.previewModeView.remove();
                this.stopListening(this.previewModeView);
                this.previewModeView = null;
            }
        },

        scrollFollow: function() {
            if(this.$el.is(':visible')) {
                const augmentationRect = this.el.getBoundingClientRect();
                const containerTop = this.scrollModel.get('top');
                const containerBottom = this.scrollModel.get('bottom');

                // Ensure that the top of the preview is at least PREVIEW_MARGIN_PIXELS from the top of the container
                // but not above the augmentation view top
                const targetTop = Math.max(containerTop + PREVIEW_MARGIN_PIXELS, augmentationRect.top);
                const margin = targetTop - augmentationRect.top;

                // Ensure that the bottom of the preview is at most PREVIEW_MARGIN_PIXELS from the bottom of the container
                const targetBottom = containerBottom - PREVIEW_MARGIN_PIXELS;
                const height = targetBottom - augmentationRect.top - margin;

                this.$previewModeContainer.css({
                    height: height,
                    marginTop: margin
                });
            }
        }
    });
});
