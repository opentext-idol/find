define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/page/search/preview-mode-view',
    'text!find/templates/app/page/search/results/results-view-augmentation.html'
], function(Backbone, $, _, PreviewModeView, template) {

    return Backbone.View.extend({

        previewModeView: null,

        template: _.template(template),

        initialize: function(options) {
            this.resultsView = options.resultsView;

            this.listenTo(this.resultsView, 'preview', function(model) {
                this.removePreviewModeView();

                this.previewModeView = new PreviewModeView({
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

            // TODO: this should be done by somebody else
            $('.right-side-container').toggle(!previewMode);

            this.$('.preview-mode-wrapper').toggleClass('hide', !previewMode);

            //making main results container smaller or bigger
            this.$('.main-results-content-container').toggleClass('col-md-6', previewMode);
            this.$('.main-results-content-container').toggleClass('col-md-12', !previewMode);

            //aligning middle and right container
            //this.$('.results-view-container .tab-pane').toggleClass('row', previewMode);

            ////aligning loading container in the middle
            //// TODO: ???
            //$('.results-view-type-list .loading-spinner').toggleClass('preview-mode-loading', previewMode);
            //
            //if(!previewMode) {
            //    var hiddenPreviewModeWrapper = this.$('.preview-mode-wrapper.hide');
            //
            //    // TODO: if we've removed the view why is this necessary
            //    //hiding and clearing the preview document's divs for future re-population
            //    hiddenPreviewModeWrapper.find('.preview-mode-document-title').empty();
            //    hiddenPreviewModeWrapper.find('.preview-mode-metadata').empty();
            //    hiddenPreviewModeWrapper.find('.preview-mode-document').empty();
            //}

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