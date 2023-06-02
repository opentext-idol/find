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
