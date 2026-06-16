/*
 * Copyright 2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'underscore',
    'jquery'
], function (_, $) {
    'use strict';

    // Automatically convert HTTP/HTTPS URLs in HTML to HTML links.
    const autoLink = function (value) {
        // use lookahead to ignore any trailing punctuation, as it may end a sentence
        const regex = /(https?:\/\/[^\s<>]+?)(?=[\.,]*(\s|<|$))/gi;

        let lastIndex = 0, match, escaped = '';
        while (match = regex.exec(value)) {
            escaped += _.escape(value.slice(lastIndex, match.index));
            const url = match[1];
            escaped += '<a href="' + _.escape(url) + '" target="_blank">' + _.escape(url) + '</a>';
            lastIndex = match.index + match[0].length;
        }

        escaped += _.escape(value.slice(lastIndex));
        return escaped;
    };

    // Prepare HTML for display by escaping all elements except a safe subset.
    // - converts newlines to <br>
    // - adds target="_blank" to links
    // - removes <cite> elements
    // This does not correctly handle nested elements with the same tag, most likely ul/li or div.
    const sanitiseHTMLInner = function (value) {
        const regex = /(<(img|chart|suggest|cite|help)( [^<>]*|)>)|(<(table|a|sup|span|ul|li|p|div|cite)( [^<>]*|)>([\s\S]*?)<\/\5>)/g;

        let lastIndex = 0, match, escaped = '';
        while (match = regex.exec(value)) {
            escaped += autoLink(value.slice(lastIndex, match.index));

            // elements that may contain content

            if (match[4]) {
                if (match[5] === 'cite') {
                    // drop cite and its contents

                } else {
                    escaped += '<' + match[5] + match[6] + '>' + sanitiseHTMLInner(match[7]) + '</' + match[5] + '>';
                }

            // self-closing elements

            } else if (match[2] === 'suggest') {
                const $tmp = $(match[0]);
                const opts = ($tmp.attr('options') || '').trim();
                if (opts) {
                    escaped += '<br>' + _.map(opts.split('|'), function (str) {
                        return '<span class="btn btn-primary btn-sm question-answer-suggestion">' + _.escape(str) + '</span>';
                    }).join(' ')
                } else {
                    const query = ($tmp.attr('query') || '').trim();
                    if (query) {
                        const label = ($tmp.attr('label') || '').trim() || query;
                        escaped += '<span class="btn btn-primary btn-sm question-answer-suggestion" data-query="' +
                            _.escape(query) + '">' + _.escape(label) + '</span>'
                    }
                }

            } else if (match[2] === 'cite') {
                // drop cite

            } else { // img, chart, help
                escaped += '<' + match[2] + ' class="safe-image"' + match[3] + '>';
            }

            lastIndex = match.index + match[0].length
        }

        escaped += autoLink(value.slice(lastIndex));
        return escaped;
    }

    // Prepare HTML for display by escaping all elements except a safe subset.
    // - converts newlines to <br>
    // - adds target="_blank" to links
    // - removes <cite> elements
    const sanitiseHTML = function (value) {
        return sanitiseHTMLInner(value).replace(/\n/g, '<br>').trim()
    };


    return {autoLink, sanitiseHTML};
});
