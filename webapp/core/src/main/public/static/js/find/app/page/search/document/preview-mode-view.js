define([
    'backbone',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'find/app/vent',
    'find/app/util/view-server-client',
    'find/app/model/document-model',
    'find/app/configuration',
    'find/app/util/database-name-resolver',
    'find/app/util/events',
    'text!find/templates/app/page/search/document/preview-mode-view.html',
    'text!find/templates/app/page/search/document/preview-mode-metadata.html',
    'text!find/templates/app/page/search/document/view-mode-document.html',
    'text!find/templates/app/page/search/document/view-media-player.html',
    'text!css/result-highlighting.css'
], function(Backbone, _, $, i18n, i18nIndexes, vent, viewClient, DocumentModel, configuration, databaseNameResolver, events, template, metaDataTemplate,
            documentTemplate, mediaTemplate, highlightingRule) {

    'use strict';

    function highlighting(innerWindow) {
        var styleEl = innerWindow.createElement('style');

        // Append style element to iframe document head
        innerWindow.head.appendChild(styleEl);

        styleEl.sheet.insertRule(highlightingRule, 0);
    }

    return Backbone.View.extend({
        className: 'well flex-column m-b-nil full-height',

        template: _.template(template),
        metaDataTemplate: _.template(metaDataTemplate),
        documentTemplate: _.template(documentTemplate),
        mediaTemplate: _.template(mediaTemplate),

        events: {
            'click .preview-mode-open-detail-button': 'openDocumentDetail',
            'click .preview-mode-highlight-query-terms': 'toggleHighlighting',
            'click .close-preview-mode': 'triggerClose'
        },

        initialize: function(options) {
            this.indexesCollection = options.indexesCollection;
            this.previewModeModel = options.previewModeModel;
            this.highlightingModel = new Backbone.Model({highlighting: false});

            var queryText = options.queryText;

            if (queryText !== '*') {
                this.queryText = queryText;
            }

            this.highlighting = this.queryText && configuration().viewHighlighting;
        },

        triggerClose: function() {
            this.pauseMedia();
            this.previewModeModel.set({document: null});
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                mmapBaseUrl: configuration().mmapBaseUrl,
                mmapUrl: this.model.get('mmapUrl'),
                viewHighlighting: this.highlighting
            }));

            this.$highlightToggle = this.$('.preview-mode-highlight-query-terms');

            this.listenTo(this.highlightingModel, 'change:highlighting', _.bind(function() {
                this.$highlightToggle.toggleClass('active', this.highlightingModel.get('highlighting'));
            }, this));

            this.$('.preview-mode-document-title').text(this.model.get('title'));

            var referenceKey = this.model.get('url') ? 'url' : 'reference';
            //noinspection JSUnresolvedFunction
            this.$('.preview-mode-metadata').html(this.metaDataTemplate({
                i18n: i18n,
                i18nIndexes: i18nIndexes,
                model: this.model,
                fields: [{
                    key: 'index',
                    value: databaseNameResolver.getDatabaseDisplayNameFromDocumentModel(this.indexesCollection, this.model)
                }, {
                    key: referenceKey,
                    value: this.model.get(referenceKey)
                }]
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

                var $iframe = this.$('.preview-document-frame');

                $iframe.on('load', _.bind(function() {
                    // Cannot execute scripts in iframe or detect error event, so look for attribute on html
                    if ($iframe.contents().find('html').data('hpeFindAuthError')) {
                        window.location.reload();
                    }

                    this.$('.view-server-loading-indicator').addClass('hidden');
                    $iframe.removeClass('hidden');

                    var $contents = $iframe.contents();

                    // View server adds script tags to rendered HTML documents, which are blocked by the application
                    // This replicates their functionality
                    $contents.find('.InvisibleAbsolute').hide();

                    var $contentDocument = $contents[0];

                    if (this.highlighting) {
                        highlighting($contentDocument);
                    }

                    this.$contentDocumentBody = $($contentDocument.body);
                    this.updateHighlighting();
                    this.listenTo(this.highlightingModel, 'change:highlighting', this.updateHighlighting);
                }, this));

                // The src attribute has to be added retrospectively to avoid a race condition
                var src = viewClient.getHref(this.model.get('reference'), this.model, this.highlighting ? this.queryText : null);
                $iframe.attr('src', src);
            }

            this.listenTo(this.model, 'remove destroy', this.triggerClose);
        },

        pauseMedia: function() {
            if (this.model.get('media') === 'video' || this.model.get('media') === 'audio') {
                this.$('.preview-media-player').get(0).pause();
            }
        },

        openDocumentDetail: function() {
            this.pauseMedia();
            vent.navigateToDetailRoute(this.model);

            events().fullPreview();
        },

        toggleHighlighting: function() {
            this.highlightingModel.set('highlighting', !this.highlightingModel.get('highlighting'));
        },

        updateHighlighting: function() {
            this.$contentDocumentBody.toggleClass('haven-search-view-document-highlighting-on', this.highlightingModel.get('highlighting'));
        }
    });

});
