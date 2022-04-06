/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'i18n!find/nls/bundle',
    'find/app/vent',
    'find/app/util/view-server-client',
    'find/app/configuration',
    'find/app/util/database-name-resolver',
    'find/app/util/events',
    'find/app/util/url-manipulator',
    'find/app/page/search/document/document-preview-helper',
    'text!find/templates/app/page/search/document/preview-mode-summary-view.html'
], function(_, $, Backbone, i18n, vent, viewClient, configuration, databaseNameResolver, events,
            urlManipulator, DocumentPreviewHelper, template) {
    'use strict';

    const highlightRuleTemplate = _.template('body.haven-search-view-document-highlighting-on .haven-search-view-document-highlighting { \n' +
        'font-weight: bold; \n' +
        '<% if (color) { %>color: <%-color%>; \n<% } %>' +
        '<% if (background) { %>background: <%-background%>; \n<% } %>' +
    '}')

    function highlighting(innerWindow) {
        const config = configuration();

        const styleEl = innerWindow.createElement('style');

        // Append style element to iframe document head
        innerWindow.head.appendChild(styleEl);

        const highlightingRule = highlightRuleTemplate({
            color: config.termHighlightColor,
            background: config.termHighlightBackground
        })
        styleEl.sheet.insertRule(highlightingRule, 0);
    }

    return Backbone.View.extend({
        className: 'well flex-column m-b-nil full-height',

        template: _.template(template),

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
            this.previewModeModel.set({ mode: null });
        },

        render: function() {
            let href;
            if(this.model.get('url')) {
                href = urlManipulator.addSpecialUrlPrefix(this.model.get('contentType'), this.model.get('url'));
            } else {
                href = viewClient.getHref(this.model, false, true);
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

            const $iframe = DocumentPreviewHelper.showPreview(
                this.$('.preview-mode-document'),
                this.model,
                this.highlighting ? this.queryText : null);

            if ($iframe) {
                $iframe.on('load', _.bind(function() {
                    const $contentDocument = $iframe.contents()[0];
                    if(this.highlighting) {
                        highlighting($contentDocument);
                    }

                    this.$contentDocumentBody = $($contentDocument.body);
                    this.updateHighlighting();
                    this.listenTo(this.highlightingModel, 'change:highlighting', this.updateHighlighting);
                }, this));
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
