define([
    'backbone',
    'underscore',
    'jquery',
    'find/app/util/tab-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/saved-searches/saved-searches-view.html'
], function(Backbone, _, $, TabView, i18n, template) {
    "use strict";

    return Backbone.View.extend({

        template: _.template(template),

        events: {
            //TODO: we need to move this into saved-search-options when we merge with dev
            'click .preview-mode-button': function(e) {
                $('.right-side-container').toggle();
                $('.preview-mode-wrapper').toggleClass('hide');
                $('.main-results-content').toggleClass('col-md-6 preview-mode');
                $('.main-results-container').removeClass('selected-document');
                $('.results-view-container .tab-pane').toggleClass('row');

                $('.preview-mode-button i').toggleClass('hp-show-preview hp-hide-preview');
                $('.preview-mode-button').toggleClass('pressed-down');

                //when we turn off preview mode, preview div gets 'hide' class and we need to reset its elements
                var hiddenPreviewModeWrapper = $('.preview-mode-wrapper.hide');

                //showing placeholder again
                hiddenPreviewModeWrapper.find('.no-document-selected-placeholder').removeClass('hide');

                //hiding and clearing the preview document's divs for future re-population
                hiddenPreviewModeWrapper.find('.preview-mode-contents').addClass('hide');
                hiddenPreviewModeWrapper.find('.preview-mode-document-title').empty();
                hiddenPreviewModeWrapper.find('.preview-mode-metadata').empty();
                hiddenPreviewModeWrapper.find('.preview-mode-document').empty();
            }
        },

        initialize: function (options) {
            this.collection = options.savedSearchesCollection;

            this.tabView = new TabView({
                collection: this.collection
            });

            this.listenTo(this.collection, 'reset update', this.updateVisibility);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.tabView.setElement(this.$('.saved-search-tab-view')).render();
            this.updateVisibility();
        },

        updateVisibility: function() {
            this.$('.saved-searches').toggleClass('hide', this.collection.isEmpty());
            this.$('.no-saved-searches').toggleClass('hide', !this.collection.isEmpty());
        }

    });
});
