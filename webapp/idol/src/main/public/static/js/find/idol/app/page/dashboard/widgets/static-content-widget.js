/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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
    './widget'
], function(_, $, Widget) {
    'use strict';

    return Widget.extend({
        initialize: function(options) {
            Widget.prototype.initialize.apply(this, arguments);
            this.html = this.widgetSettings.html;
        },

        render: function() {
            Widget.prototype.render.apply(this);

            this.$content.html(this.html);
        },

        exportData: function() {
            const nodes = [];

            const fontScale = 10 / 16;

            // Try to traverse the content in a similar way that HTML would parse it, making bold text etc.
            // We can't just use $el.text() since that loses all the formatting.
            function traverse($el) {
                _.each(_.filter($el.contents(), function(dom) {
                    return dom.nodeType === Node.TEXT_NODE || dom.nodeType === Node.ELEMENT_NODE
                }), function(dom) {
                    if(dom.nodeType === Node.TEXT_NODE) {
                        const last = _.last(nodes);
                        if(last && last.text.slice(-1) !== '\n' && $el.css('display') !== 'inline') {
                            last.text += '\n';
                        }

                        const trimmedText = dom.nodeValue.trim();

                        if(trimmedText) {
                            // text node, add with the current CSS
                            const fontFamily = $el.css('font-family');
                            const fontWeight = $el.css('font-weight');
                            nodes.push({
                                // we want to coalesce all whitespace into a single whitespace (to match HTML)
                                text: dom.nodeValue.replace(/\s+/, ' '),
                                fontSize: Math.round(parseInt($el.css('font-size')) * fontScale),
                                // font weight might be a number > 400, bold/bolder etc. or built into font family
                                bold: fontWeight > 400 || /bold/.test(fontWeight) || /Bold/i.test(fontFamily),
                                // italic may be a property of the font style, font weight or font-family
                                italic: /italic/.test($el.css('font-style')) || /Italic/i.test(fontFamily)
                            })
                        }
                    } else if(dom.nodeType === Node.ELEMENT_NODE) {
                        traverse($(dom))
                    }
                })
            }

            traverse(this.$content);

            return {
                data: {
                    text: nodes
                },
                type: 'text'
            };
        }
    });
});
