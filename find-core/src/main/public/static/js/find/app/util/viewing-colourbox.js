/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'underscore',
    'find/app/model/document-model',
    'text!find/templates/app/page/colorbox-controls.html',
    'text!find/templates/app/page/view/document-content.html',
    'text!find/templates/app/page/view/audio-player.html',
    'text!find/templates/app/page/view/video-player.html',
    'text!find/templates/app/page/view/view-document.html',
    'i18n!find/nls/bundle'
], function ($, _, DocumentModel, colorboxControlsTemplate, documentContentTemplateString, audioPlayerTemplateString, videoPlayerTemplateString, viewDocumentTemplateString, i18n) {

    'use strict';

    var SIZE = '90%';
    var $window = $(window);
    var isUrlRegex = /^https?:\/\/|^file:\/\/|^file:\\\\/;

    var documentContentTemplate = _.template(documentContentTemplateString);
    var audioPlayerTemplate = _.template(audioPlayerTemplateString);
    var videoPlayerTemplate = _.template(videoPlayerTemplateString);
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

    function colorboxArguments(options, isUrl) {
        var args = {
            current: '{current} of {total}',
            height: '70%',
            iframe: false,
            rel: options.grouping,
            width: '70%',
            onClosed: function () {
                $window.off('resize', onResize);
            },
            onComplete: _.bind(function () {
                $('#cboxPrevious, #cboxNext').remove(); //removing default colorbox nav buttons

                var $iframe = $('.view-server-page');

                $iframe.on('load', function () {
                    $('.view-server-loading-indicator').addClass('hidden');
                    $('.view-server-page').removeClass('hidden');

                    // View server adds script tags to rendered HTML documents, which are blocked by the application
                    // This replicates their functionality
                    $iframe.contents().find('.InvisibleAbsolute').hide();
                });

                // Adding the source attribute after the colorbox has loaded prevents the iframe from loading
                // a very quick response (such as an error) before the listener is attached
                $iframe.attr("src", options.href);

                $window.resize(onResize);
            }, this)
        };

        var contentType = options.model.get('contentType') || '';

        var identifyMedia = function (mediaType) {
            return contentType.indexOf(mediaType) === 0;
        };

        var url = options.model.get('url');

        var content;
        if (identifyMedia('audio') && url) {
            content = audioPlayerTemplate({
                url: url,
                offset: options.model.get('offset')
            });
        } else if (identifyMedia('video') && url) {
            content = videoPlayerTemplate({
                url: url,
                offset: options.model.get('offset')
            });
        } else {
            content = documentContentTemplate({
                i18n: i18n
            });
        }

        args.html = viewDocumentTemplate({
            src: isUrl ?  options.model.get('reference') : options.href,
            i18n: i18n,
            model: options.model,
            content: content
        });

        return args;
    }

    function isURL(reference) {
        return isUrlRegex.test(reference);
    }

    function nearNativeOrTab(options, reference, targetNode, triggerNode) {
        var isUrl = isURL(reference);
        var colorboxArgs = colorboxArguments(options, isUrl);

        if (triggerNode) {
            triggerNode.colorbox(colorboxArgs);
        }

        // web documents should open the original document in a new tab
        if (isUrl) {
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