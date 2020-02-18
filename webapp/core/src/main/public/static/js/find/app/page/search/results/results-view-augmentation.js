/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'text!find/templates/app/page/search/results/results-view-augmentation.html'
], function(_, $, Backbone, viewHtml) {
    'use strict';

    // We always want a gap between the preview well and the container
    const PREVIEW_MARGIN_PIXELS = 10;

    return Backbone.View.extend({
        // abstract
        PreviewModeView: null,

        initialize: function(options) {
            this.resultsView = options.resultsView;
            this.queryModel = options.queryModel;
            this.scrollModel = options.scrollModel;

            // Tracks document currently being previewed in the "documents" attribute
            this.previewModeModel = options.previewModeModel;

            this.listenTo(this.previewModeModel, 'change:document', function(model, documentModel) {
                if(documentModel) {
                    this.removePreviewModeView();

                    this.previewModeView = new this.PreviewModeView({
                        model: documentModel,
                        documentRenderer: options.documentRenderer,
                        previewModeModel: this.previewModeModel,
                        queryText: this.queryModel.get('queryText'),
                        indexesCollection: options.indexesCollection,
                        mmapTab: options.mmapTab
                    });

                    this.$previewModeContainer.append(this.previewModeView.$el);
                    this.previewModeView.render();
                    this.scrollFollow();
                }

                this.togglePreviewMode(!!documentModel);
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
