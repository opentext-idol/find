/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'settings/js/widget',
    'text!find/templates/app/page/settings/widget.html',
    'text!find/templates/app/page/settings/motd-widget.html'
], function(_, Widget, widgetTemplate, template) {
    'use strict';

    return Widget.extend({
        widgetTemplate: _.template(widgetTemplate),
        template: _.template(template),

        events: {
            'change .message-input': 'onTextChange',
            'keyup .message-input': 'onTextChange',
            'change .status-input': 'onStatusChange'
        },

        onTextChange: function(){
            $('.find-navbar-motd').text(this.$message.val());
        },

        onStatusChange: function(){
            $('.find-navbar-motd').attr('class', 'find-navbar-motd ' + this.$status.val());
        },

        initialize: function() {
            Widget.prototype.initialize.apply(this, arguments);
        },

        render: function() {
            Widget.prototype.render.apply(this);

            this.$content.html(this.template({
                strings: this.strings
            }));

            this.$message = this.$('.message-input');
            this.$status = this.$('.status-input');
        },

        getConfig: function() {
            return {
                message: this.$message.val(),
                cssClass: this.$status.val()
            }
        },

        updateConfig: function(config) {
            this.$message.val(config.message);
            this.$status.val(config.cssClass);
            this.onTextChange();
            this.onStatusChange();
        }
    });
});
