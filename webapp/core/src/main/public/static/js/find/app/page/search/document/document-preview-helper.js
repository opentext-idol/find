/*
 * (c) Copyright 2020 Micro Focus or one of its affiliates.
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
    'i18n!find/nls/bundle',
    'find/app/util/url-manipulator',
    'find/app/util/view-server-client',
    'text!find/templates/app/page/search/document/view-mode-document.html',
    'text!find/templates/app/page/search/document/view-media-player.html'
], function(_, i18n, urlManipulator, viewClient, documentTemplateText, mediaTemplateText) {
    'use strict';

    const documentTemplate = _.template(documentTemplateText);
    const mediaTemplate = _.template(mediaTemplateText);

    const DocumentPreviewHelper = {};

    /**
     * Show a document preview using View Server.
     *
     * @param $parentEl HTML element to show the preview in
     * @param model Model for the document to preview
     * @param highlighting If/how to highlight query terms - see viewClient.getHref
     * @returns jQuery element for the created iframe containing the document preview
     */
    DocumentPreviewHelper.showPreview = function ($parentEl, model, highlighting) {
        const previewTemplate = model.getPreviewTemplate();

        if(model.isMedia()) {
            $parentEl.html(mediaTemplate({ i18n: i18n, model: model }));
            return null;

        } else if(previewTemplate) {
            $parentEl.html(previewTemplate);
            return null;

        } else {
            $parentEl.html(documentTemplate({
                i18n: i18n
            }));

            const $iframe = $parentEl.find('.preview-document-frame');

            $iframe.on('load', function() {
                // Cannot execute scripts in iframe or detect error event, so look for attribute on html
                if($iframe.contents().find('html').data('hpeFindAuthError')) {
                    window.location.reload();
                }

                $parentEl.find('.view-server-loading-indicator').addClass('hidden');
                $iframe.removeClass('hidden');

                // View server adds script tags to rendered PDF documents, which are blocked by the application
                // This replicates their functionality
                $iframe.contents().find('.InvisibleAbsolute').hide();
            });

            // The src attribute has to be added retrospectively to avoid a race condition
            const url = viewClient.getHref(model, highlighting, false);
            const urlWithHashFragment = urlManipulator.appendHashFragment(model, url);
            $iframe.attr('src', urlWithHashFragment);

            return $iframe;
        }
    }

    return DocumentPreviewHelper;
});
