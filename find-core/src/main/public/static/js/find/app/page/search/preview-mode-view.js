define([
    'backbone',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'find/app/model/document-model',
    'text!find/templates/app/page/search/preview-mode-view.html',
    'text!find/templates/app/page/search/preview-mode-metadata.html',
    'text!find/templates/app/page/search/preview-mode-document.html',
    'text!find/templates/app/page/view/media-player.html'
], function(Backbone, _, $, i18n, DocumentModel, template, metaDataTemplate, documentTemplate, mediaTemplate) {
    "use strict";

    return Backbone.View.extend({
        template: _.template(template),
        metaDataTemplate: _.template(metaDataTemplate),
        documentTemplate: _.template(documentTemplate),
        mediaTemplate: _.template(mediaTemplate),

        render: function() {
            this.$el.html(this.template({
                i18n:i18n
            }));

            var offset     = this.$el.offset();
            var topPadding = 15;

            var $main = $('.main-content');
            $main.scroll(_.bind(function() {
                if ($main.scrollTop() > offset.top) {
                    this.$el.stop().animate({
                        marginTop: $main.scrollTop() - offset.top + topPadding
                    });
                } else {
                    this.$el.stop().animate({
                        marginTop: 0
                    });
                }
            }, this));
        },

        renderView: function(args) {

            var model = args.model;

            this.$('.preview-mode-metadata').html(this.metaDataTemplate({
                i18n:i18n,
                model: model,
                arrayFields: DocumentModel.ARRAY_FIELDS,
                dateFields: DocumentModel.DATE_FIELDS,
                fields: ['index', 'reference', 'contentType', 'url']
            }));

            var $preview = this.$('.preview-mode-document');

            if (args.media) {
                $preview.html(this.mediaTemplate({
                    i18n: i18n,
                    offset: args.offset,
                    media: args.media,
                    url: args.url
                }));
            } else {
                $preview.html(this.documentTemplate({i18n: i18n}));

                var $viewServerPage = this.$('.preview-document-frame');

                $viewServerPage.on('load', _.bind(function() {
                    this.$('.view-server-loading-indicator').addClass('hidden');
                    $viewServerPage.removeClass('hidden');
                }, this));

                $viewServerPage.attr("src", args.src);
            }
        }
    });

});
