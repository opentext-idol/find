define([
    'backbone',
    'jquery',
    'underscore',
    'text!find/templates/app/page/search/results/results-view-augmentation.html'
], function(Backbone, $, _, template) {

    return Backbone.View.extend({

        // to be overridden
        PreviewModeView: null,

        template: _.template(template),

        initialize: function(options) {
            this.resultsView = options.resultsView;

            this.listenTo(this.resultsView, 'preview', function(model) {
                this.removePreviewModeView();

                this.previewModeView = new this.PreviewModeView({
                    model: model
                });

                this.listenTo(this.previewModeView, 'close-preview', function() {
                    this.togglePreviewMode(false);
                });

                this.$('.preview-mode-container').append(this.previewModeView.$el);
                this.previewModeView.render();

                this.togglePreviewMode(true);
            }, this);

            this.listenTo(this.resultsView, 'close-preview', function() {
                this.togglePreviewMode(false);
            });
        },

        render: function() {
            this.$el.html(this.template());

            this.resultsView.setElement(this.$('.main-results-content')).render();
        },

        togglePreviewMode: function(previewMode) {
            if(!previewMode) {
                this.resultsView.removeHighlighting();
            }

            this.trigger('rightSideContainerHideToggle', !previewMode);

            this.$('.preview-mode-wrapper').toggleClass('hide', !previewMode);

            //making main results container smaller or bigger
            this.$('.main-results-content-container').toggleClass('col-md-6', previewMode);
            this.$('.main-results-content-container').toggleClass('col-md-12', !previewMode);
        },

        remove: function () {
            Backbone.View.prototype.remove.call(this);
            this.removePreviewModeView();
        },

        removePreviewModeView: function() {
            if (this.previewModeView) {
                this.previewModeView.remove();
                this.stopListening(this.previewModeView);
                this.previewModeView = null;
            }
        }

    });

});