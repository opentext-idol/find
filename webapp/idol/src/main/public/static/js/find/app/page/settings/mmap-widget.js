/*
 * Copyright 2016-2017 Open Text.
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

const _ = require('underscore');
const Widget = require('settings/js/widget');
const EnableView = require('find/app/page/settings/enable-view');
const widgetTemplate = require('find/templates/app/page/settings/widget.html');
const template = require('find/templates/app/page/settings/mmap-widget.html');

module.exports = Widget.extend({
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

