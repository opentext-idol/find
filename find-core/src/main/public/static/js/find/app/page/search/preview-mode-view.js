define([
    'backbone',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'find/app/util/view-server-client',
    'find/app/model/document-model',
    'text!find/templates/app/page/search/preview-mode-view.html',
    'text!find/templates/app/page/search/preview-mode-metadata.html',
    'text!find/templates/app/page/search/preview-mode-document.html',
    'text!find/templates/app/page/view/media-player.html'
], function(Backbone, _, $, i18n, viewClient, DocumentModel, template, metaDataTemplate, documentTemplate, mediaTemplate) {
    "use strict";

    var mediaTypes = ['audio', 'video'];

    function scrollFollow() {
        if (this.$el.offsetParent().offset().top < 0) {
            this.$el.css('margin-top', Math.abs(+this.$el.offsetParent().offset().top) + 15);
        } else {
            this.$el.css('margin-top', 0);
        }

        if (!this.media) {
            var $viewServerPage = this.$('.preview-document-frame');
            $viewServerPage.css('height', $(window).height() - $viewServerPage.offset().top - 30 - this.$('.preview-mode-metadata').height())
        }
    }

    return Backbone.View.extend({
        template: _.template(template),
        metaDataTemplate: _.template(metaDataTemplate),
        documentTemplate: _.template(documentTemplate),
        mediaTemplate: _.template(mediaTemplate),

        events: {
            'click .close-preview-mode': function() {
                this.trigger('close-preview');
            }
        },

        initialize: function() {
            this.scrollFollow = _.bind(scrollFollow, this);
        },

        // TODO: this should do the stuff in render view
        render: function() {
            this.$el.html(this.template({
                i18n:i18n
            }));

            this.$('.preview-mode-document-title').text(this.model.get('title'));

            var contentType = this.model.get('contentType') || '';

            this.media = _.find(mediaTypes, function (mediaType) {
                return contentType.indexOf(mediaType) === 0;
            });

            var url = this.model.get('url');

            this.$('.preview-mode-metadata').html(this.metaDataTemplate({
                i18n:i18n,
                model: this.model,
                arrayFields: DocumentModel.ARRAY_FIELDS,
                dateFields: DocumentModel.DATE_FIELDS,
                fields: ['index', 'reference', 'contentType', 'url']
            }));

            var $preview = this.$('.preview-mode-document');

            if (this.media && url) {
                $preview.html(this.mediaTemplate({
                    i18n: i18n,
                    model: this.model
                }));
            } else {
                $preview.html(this.documentTemplate({
                    i18n: i18n
                }));

                this.$iframe = this.$('.preview-document-frame');

                this.$iframe.on('load', _.bind(function() {
                    this.$('.view-server-loading-indicator').addClass('hidden');
                    this.$iframe.removeClass('hidden');
                    this.$iframe.css('height', $(window).height() - this.$iframe.offset().top - 30 - this.$('.preview-mode-metadata').height())
                }, this));

                // The src attribute has to be added retrospectively to avoid a race condition
                this.$iframe.attr("src", viewClient.getHref(this.model.get('reference'), this.model.get('index'), this.model.get('domain')));
                this.$iframe.css('height', $(window).height() - $preview.offset().top - 30 - this.$('.preview-mode-metadata').height());
            }
            
            this.scrollFollow();

            $('.main-content').scroll(this.scrollFollow);
        },

        remove: function() {
            Backbone.View.prototype.remove.call(this);

            $('.main-content').off('scroll', this.scrollFollow);
        }
    });

});
