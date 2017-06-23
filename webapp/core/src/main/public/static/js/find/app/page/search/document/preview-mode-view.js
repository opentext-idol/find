/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'find/app/vent',
    'find/app/util/view-server-client',
    'find/app/model/document-model',
    'find/app/configuration',
    'find/app/util/database-name-resolver',
    'find/app/util/events',
    'find/app/util/url-manipulator',
    'text!find/templates/app/page/search/document/preview-mode-view.html',
    'text!find/templates/app/page/search/document/view-mode-document.html',
    'text!find/templates/app/page/search/document/view-media-player.html',
    'text!css/result-highlighting.css'
], function(_, $, Backbone, i18n, i18nIndexes, vent, viewClient, DocumentModel, configuration, databaseNameResolver,
            events, urlManipulator, template, documentTemplate, mediaTemplate, highlightingRule) {
    'use strict';

    function highlighting(innerWindow) {
        const styleEl = innerWindow.createElement('style');

        // Append style element to iframe document head
        innerWindow.head.appendChild(styleEl);

        styleEl.sheet.insertRule(highlightingRule, 0);
    }

    return Backbone.View.extend({
        className: 'well flex-column m-b-nil full-height',

        template: _.template(template),
        documentTemplate: _.template(documentTemplate),
        mediaTemplate: _.template(mediaTemplate),

        events: {
            'click .preview-mode-open-detail-button': 'openDocumentDetail',
            'click .preview-mode-highlight-query-terms': 'toggleHighlighting',
            'click .preview-mode-mmap-button': function() {
                this.mmapTab.open(this.model.attributes);
            },
            'click .close-preview-mode': 'triggerClose'
        },

        initialize: function(options) {
            this.indexesCollection = options.indexesCollection;
            this.previewModeModel = options.previewModeModel;
            this.mmapTab = options.mmapTab;
            this.documentRenderer = options.documentRenderer;

            this.highlightingModel = new Backbone.Model({highlighting: false});

            const queryText = options.queryText;

            if(queryText !== '*') {
                this.queryText = queryText;
            }

            this.highlighting = this.queryText && configuration().viewHighlighting;
        },

        triggerClose: function() {
            this.pauseMedia();
            this.previewModeModel.set({document: null});
        },

        render: function() {
            let href;
            if(this.model.get('url')) {
                href = urlManipulator.addSpecialUrlPrefix(this.model.get('contentType'), this.model.get('url'));
            }

            const referenceKey = this.model.get('url') ? 'url' : 'reference';
            let reference = this.model.get(referenceKey);

            this.$el.html(this.template({
                i18n: i18n,
                href: href,
                mmap: this.mmapTab.supported(this.model.attributes),
                viewHighlighting: this.highlighting,
                url: reference,
                mediaType: this.model.get('media') + '-container'
            }));

            this.$highlightToggle = this.$('.preview-mode-highlight-query-terms');

            this.listenTo(this.highlightingModel, 'change:highlighting', _.bind(function() {
                this.$highlightToggle.toggleClass('active', this.highlightingModel.get('highlighting'));
            }, this));

            this.$('.preview-mode-document-title').prepend(this.model.get('title'));
            this.$('.preview-mode-document-database').text(
                databaseNameResolver.getDatabaseDisplayNameFromDocumentModel(this.indexesCollection, this.model)
            );
            this.$('.preview-mode-document-url').text(reference).toggleClass('hide', !reference);

            //noinspection JSUnresolvedFunction
            this.$('.preview-mode-metadata').html(this.documentRenderer.renderPreviewMetadata(this.model));

            const $preview = this.$('.preview-mode-document');

            if(this.model.isMedia()) {
                $preview.html(this.mediaTemplate({
                    i18n: i18n,
                    model: this.model
                }));
            } else {
                $preview.html(this.documentTemplate({
                    i18n: i18n
                }));

                const $iframe = this.$('.preview-document-frame');

                $iframe.on('load', _.bind(function() {
                    // Cannot execute scripts in iframe or detect error event, so look for attribute on html
                    if($iframe.contents().find('html').data('hpeFindAuthError')) {
                        window.location.reload();
                    }

                    this.$('.view-server-loading-indicator').addClass('hidden');
                    $iframe.removeClass('hidden');

                    const $contents = $iframe.contents();

                    // View server adds script tags to rendered HTML documents, which are blocked by the application
                    // This replicates their functionality
                    $contents.find('.InvisibleAbsolute').hide();

                    const $contentDocument = $contents[0];

                    if(this.highlighting) {
                        highlighting($contentDocument);
                    }

                    this.$contentDocumentBody = $($contentDocument.body);
                    this.updateHighlighting();
                    this.listenTo(this.highlightingModel, 'change:highlighting', this.updateHighlighting);
                }, this));

                // The src attribute has to be added retrospectively to avoid a race condition
                const src = viewClient.getHref(this.model.get('reference'),
                    this.model,
                    this.highlighting
                        ? this.queryText
                        : null);

                const srcWithHashFragment = urlManipulator.appendHashFragment(this.model, src);

                $iframe.attr('src', srcWithHashFragment);
            }

            this.listenTo(this.model, 'remove destroy', this.triggerClose);
        },

        pauseMedia: function() {
            if(this.model.get('media') === 'video' || this.model.get('media') === 'audio') {
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
            this.$contentDocumentBody.toggleClass('haven-search-view-document-highlighting-on',
                this.highlightingModel.get('highlighting'));
        }
    });
});
