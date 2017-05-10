/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'settings/js/widget',
    'find/app/page/settings/enable-view',
    'text!find/templates/app/page/settings/widget.html',
    'text!find/templates/app/page/settings/mmap-widget.html'
], function(_, Widget, EnableView, widgetTemplate, template) {
    'use strict';

    return Widget.extend({
        widgetTemplate: _.template(widgetTemplate),
        template: _.template(template),

        initialize: function() {
            Widget.prototype.initialize.apply(this, arguments);

            this.enableView = new EnableView({
                className: 'form-group m-t-sm',
                enableIcon: 'fa fa-file',
                strings: this.strings
            })
        },

        render: function() {
            Widget.prototype.render.apply(this);

            this.$content.html(this.template({
                strings: this.strings
            }));

            this.enableView.render();

            this.$content.append(this.enableView.$el);

            this.$url = this.$('.mmap-url-input');
        },

        getConfig: function() {
            return {
                baseUrl: this.$url.val(),
                enabled: this.enableView.getConfig()
            }
        },

        updateConfig: function(config) {
            this.enableView.updateConfig(config.enabled);

            this.$url.val(config.baseUrl);
        }
    });
});
