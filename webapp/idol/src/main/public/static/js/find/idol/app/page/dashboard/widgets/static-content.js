/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    './widget'
], function(Widget) {
    'use strict';

    return Widget.extend({
        initialize: function(options) {
            Widget.prototype.initialize.apply(this, arguments);

            this.html = options.widgetSettings.html;
        },

        render: function() {
            Widget.prototype.render.apply(this);

            this.$content.html(this.html);
        },

        exportPPTData: function(){
            var nodes = [];

            var fontScale = 10 / 16;

            // Try to traverse the content in a similar way that HTML would parse it, making bold text etc.
            // We can't just use $el.text() since that loses all the formatting.
            function traverse($el) {
                _.each(_.filter($el.contents(), function(dom){
                    return dom.nodeType === Node.TEXT_NODE || dom.nodeType === Node.ELEMENT_NODE
                }), function(dom, idx, all){
                    if (dom.nodeType === Node.TEXT_NODE) {
                        var last = _.last(nodes);
                        if (last && last.text.slice(-1) !== '\n' && $el.css('display') !== 'inline') {
                            last.text += '\n';
                        }

                        var trimmedText = dom.nodeValue.trim();

                        if (trimmedText) {
                            // text node, add with the current CSS
                            var fontFamily = $el.css('font-family');
                            var fontWeight = $el.css('font-weight');
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
                    }
                    else if (dom.nodeType === Node.ELEMENT_NODE) {
                        traverse($(dom))
                    }
                })
            }

            traverse(this.$content)

            return {
                data: {
                    text: nodes
                },
                type: 'text'
            };
        }
    });
});
