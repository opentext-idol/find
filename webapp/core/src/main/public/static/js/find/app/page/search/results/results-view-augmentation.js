define([
    'backbone',
    'jquery',
    'underscore',
    'text!find/templates/app/page/search/results/results-view-augmentation.html'
], function(Backbone, $, _, viewHtml) {

    // We always want a gap between the preview well and the container
    var PREVIEW_MARGIN_PIXELS = 10;

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
                if (documentModel) {
                    this.removePreviewModeView();

                    this.previewModeView = new this.PreviewModeView({
                        model: documentModel,
                        previewModeModel: this.previewModeModel,
                        queryText: this.queryModel.get('queryText'),
                        indexesCollection: options.indexesCollection
                    });

                    this.$previewModeContainer.append(this.previewModeView.$el);
                    this.previewModeView.render();
                    this.scrollFollow();

                    this.togglePreviewMode(true);
                } else {
                    this.togglePreviewMode(false);
                }
            });

            this.listenTo(this.scrollModel, 'change', this.scrollFollow);
        },

        render: function() {
            this.$el.html(viewHtml);

            this.$previewModeContainer = this.$('.preview-mode-container');

            this.resultsView.setElement(this.$('.main-results-content')).render();
            this.scrollFollow();
        },

        togglePreviewMode: function(previewMode) {
            this.trigger('rightSideContainerHideToggle', !previewMode);

            this.$('.preview-mode-wrapper').toggleClass('hide', !previewMode);

            //making main results container smaller or bigger
            this.$('.main-results-content-container').toggleClass('col-md-6', previewMode);
            this.$('.main-results-content-container').toggleClass('col-md-12', !previewMode);
        },

        remove: function () {
            this.resultsView.remove();
            this.removePreviewModeView();
            Backbone.View.prototype.remove.call(this);
        },

        removePreviewModeView: function() {
            if (this.previewModeView) {
                this.previewModeView.remove();
                this.stopListening(this.previewModeView);
                this.previewModeView = null;
            }
        },

        scrollFollow: function() {
            if (this.$el.is(':visible')) {
                var augmentationRect = this.el.getBoundingClientRect();
                var containerTop = this.scrollModel.get('top');
                var containerBottom = this.scrollModel.get('bottom');

                // Ensure that the top of the preview is at least PREVIEW_MARGIN_PIXELS from the top of the container
                // but not above the augmentation view top
                var targetTop = Math.max(containerTop + PREVIEW_MARGIN_PIXELS, augmentationRect.top);
                var margin = targetTop - augmentationRect.top;

                // Ensure that the bottom of the preview is at most PREVIEW_MARGIN_PIXELS from the bottom of the container
                var targetBottom = containerBottom - PREVIEW_MARGIN_PIXELS;
                var height = targetBottom - augmentationRect.top - margin;

                this.$previewModeContainer.css({
                    height: height,
                    marginTop: margin
                });
            }
        }
    });

});
