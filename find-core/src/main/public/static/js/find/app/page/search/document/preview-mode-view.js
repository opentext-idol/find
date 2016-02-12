define([
    'backbone',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'find/app/vent',
    'find/app/util/view-server-client',
    'find/app/model/document-model',
    'find/app/configuration',
    'text!find/templates/app/page/search/document/preview-mode-view.html',
    'text!find/templates/app/page/search/document/preview-mode-metadata.html',
    'text!find/templates/app/page/search/document/preview-mode-document.html',
    'text!find/templates/app/page/view/media-player.html'
], function(Backbone, _, $, i18n, vent, viewClient, DocumentModel, configuration, template, metaDataTemplate, documentTemplate, mediaTemplate) {
    'use strict';

    function scrollFollow() {
        if (this.$el.offsetParent().offset().top < 0) {
            this.$el.css('margin-top', Math.abs(this.$el.offsetParent().offset().top) + 15);
        } else {
            this.$el.css('margin-top', 0);
        }

        if(this.$iframe) {
            this.$iframe.css('height', $(window).height() - this.$iframe.offset().top - 30 - this.$('.preview-mode-metadata').height());
        }
    }

    return Backbone.View.extend({
        template: _.template(template),
        metaDataTemplate: _.template(metaDataTemplate),
        documentTemplate: _.template(documentTemplate),
        mediaTemplate: _.template(mediaTemplate),

        //to be overridden
        generateDetailRoute: null,

        events: {
            'click .preview-mode-open-detail-button': 'openDocumentDetail',
            'click .close-preview-mode': function() {
                this.trigger('close-preview');
            }
        },

        $iframe: null,

        initialize: function() {
            this.scrollFollow = _.bind(scrollFollow, this);
        },

        render: function() {
            this.$el.html(this.template({
                i18n:i18n,
                mmapBaseUrl: configuration().mmapBaseUrl,
                mmapUrl: this.model.get('mmapUrl')
            }));

            this.$('.preview-mode-document-title').text(this.model.get('title'));

            this.$('.preview-mode-metadata').html(this.metaDataTemplate({
                i18n:i18n,
                model: this.model,
                arrayFields: DocumentModel.ARRAY_FIELDS,
                dateFields: DocumentModel.DATE_FIELDS,
                fields: ['index', 'reference', 'contentType', 'url']
            }));

            var $preview = this.$('.preview-mode-document');

            if (this.model.isMedia()) {
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
                var src = viewClient.getHref(this.model.get('reference'), this.model);
                this.$iframe.attr('src', src);
                this.$iframe.css('height', $(window).height() - $preview.offset().top - 30 - this.$('.preview-mode-metadata').height());
            }

            this.scrollFollow();

            $('.main-content').scroll(this.scrollFollow);
        },

        remove: function() {
            Backbone.View.prototype.remove.call(this);

            $('.main-content').off('scroll', this.scrollFollow);
        },

        openDocumentDetail: function () {
            vent.navigate(this.generateDetailRoute());
        }
    });

});
