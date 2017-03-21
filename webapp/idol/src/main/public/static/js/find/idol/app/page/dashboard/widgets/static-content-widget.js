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

            this.html = this.widgetSettings.html;
        },

        render: function() {
            Widget.prototype.render.apply(this);

            this.$content.html(this.html);
        }
    });
});
