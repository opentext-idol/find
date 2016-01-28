/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'underscore',
    'find/app/model/document-model',
    'text!find/templates/app/page/colorbox-controls.html',
    'text!find/templates/app/page/view/media-player.html',
    'text!find/templates/app/page/view/view-document.html',
    'i18n!find/nls/bundle'
], function ($, _, DocumentModel, colorboxControlsTemplate, mediaPlayerTemplateString, viewDocumentTemplateString, i18n) {
    "use strict";

    var SIZE = '90%';
    var window = $(window);
    var mediaTypes = ['audio', 'video'];
    var isUrlRegex = /^https?:\/\//;

    var mediaPlayerTemplate = _.template(mediaPlayerTemplateString);
    var viewDocumentTemplate = _.template(viewDocumentTemplateString);

    function fancyButtonOverride() {
        $('#colorbox').append(_.template(colorboxControlsTemplate));
        $('.nextBtn').on('click', handleNextResult);
        $('.prevBtn').on('click', handlePrevResult);
    }

    function handlePrevResult() {
        $.colorbox.prev();
    }

    function handleNextResult() {
        $.colorbox.next();
    }

    function onResize() {
        $.colorbox.resize({width: SIZE, height: SIZE});
    }

    function colorboxArguments(options) {
        var args = {
            current: '{current} of {total}',
            height: '70%',
            iframe: false,
            rel: options.grouping,
            width: '70%',
            onClosed: function () {
                window.off('resize', onResize);
            },
            onComplete: _.bind(function () {
                $('#cboxPrevious, #cboxNext').remove(); //removing default colorbox nav buttons

                var viewServerPage = $('.view-server-page');

                viewServerPage.on('load', function () {
                    $('.view-server-loading-indicator').addClass('hidden');
                    $('.view-server-page').removeClass('hidden');
                });

                // Adding the source attribute after the colorbox has loaded prevents the iframe from loading
                // a very quick response (such as an error) before the listener is attached
                viewServerPage.attr("src", options.href);

                window.resize(onResize);
            }, this)
        };

        var contentType = options.model.get('contentType') || '';

        var media = _.find(mediaTypes, function (mediaType) {
            return contentType.indexOf(mediaType) === 0;
        });

        var url = options.model.get('url');

        if (media && url) {
            args.html = mediaPlayerTemplate({
                media: media,
                url: url,
                offset: options.model.get('offset')
            });
        } else {
            args.html = viewDocumentTemplate({
                src: options.href,
                i18n: i18n,
                model: options.model,
                arrayFields: DocumentModel.ARRAY_FIELDS,
                dateFields: DocumentModel.DATE_FIELDS,
                fields: ['index', 'reference', 'contentType', 'url']
            });
        }

        return args;
    }

    function isURL(reference) {
        return isUrlRegex.test(reference);
    }

    function nearNativeOrTab(options, reference, targetNode, triggerNode) {
        var colorboxArgs = colorboxArguments(options);

        if (triggerNode) {
            triggerNode.colorbox(colorboxArgs);
        }

        // web documents should open the original document in a new tab
        if (isURL(reference)) {
            targetNode.attr({
                href: reference,
                target: "_blank"
            });
        } else if (triggerNode) {
            targetNode.click(function(e) {
                e.preventDefault();
                triggerNode.colorbox(_.extend({open: true}, colorboxArgs));
            });
        } else {
            targetNode.colorbox(colorboxArgs);
        }
    }

    return {
        fancyButtonOverride: fancyButtonOverride,
        nearNativeOrTab: nearNativeOrTab
    }
});