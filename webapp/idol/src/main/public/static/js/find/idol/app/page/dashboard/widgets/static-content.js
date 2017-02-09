/*
 * Copyright 2014-2017 Hewlett-Packard Development Company, L.P.
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
            Widget.prototype.render.apply(this, arguments);

            this.$content.html(this.html);
        },

        exportPPTData: function(){
            var text = this.$content.text();

            // Depending on how complicated we need to make this parser, we could also handle font, italics, etc.
            return {
                data: {
                    text: [ {
                        text: text
                    } ]
                },
                type: 'text'
            };
        }
    });

});