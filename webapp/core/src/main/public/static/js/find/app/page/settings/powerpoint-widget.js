/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'settings/js/widget',
    'text!find/templates/app/page/settings/widget.html',
    'text!find/templates/app/page/settings/powerpoint-widget.html',
    'underscore'
], function($, Widget, widgetTemplate, template, _) {
    'use strict';

    return Widget.extend({
        widgetTemplate: _.template(widgetTemplate),
        template: _.template(template),

        initialize: function() {
            Widget.prototype.initialize.apply(this, arguments);
        },

        render: function() {
            Widget.prototype.render.apply(this, arguments);

            this.$content.html(this.template({
                strings: this.strings
            }));

            this.$templateFile = this.$('.template-file-input');
        },

        getConfig: function() {
            return {
                templateFile: this.$templateFile.val()
            }
        },

        updateConfig: function(config) {
            this.$templateFile.val(config ? config.templateFile : '');
        }
    });
});
