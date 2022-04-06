/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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
    'jquery',
    'settings/js/widget',
    'find/app/page/settings/enable-view',
    'text!find/templates/app/page/settings/widget.html',
    'text!find/templates/app/page/settings/saved-search-widget.html',
    'underscore'
], function($, Widget, EnableView, widgetTemplate, template, _) {
    'use strict';

    return Widget.extend({
        widgetTemplate: _.template(widgetTemplate),
        template: _.template(template),

        initialize: function() {
            Widget.prototype.initialize.apply(this, arguments);

            this.enableView = new EnableView({
                className: 'form-group m-t-sm',
                enableIcon: 'fa fa-file',
                strings: {
                    enable: this.strings.enablePolling,
                    enabled: this.strings.pollingEnabled,
                    disable: this.strings.disablePolling,
                    disabled: this.strings.pollingDisabled
                }
            })
        },

        render: function() {
            Widget.prototype.render.apply(this, arguments);

            this.$content.html(this.template({
                strings: this.strings
            }));

            this.enableView.render();

            this.$content.append(this.enableView.$el);

            this.$pollingInterval = this.$('.saved-search-polling-interval');
        },

        getConfig: function() {
            return {
                pollForUpdates: this.enableView.getConfig(),
                pollingInterval: this.$pollingInterval.val()
            }
        },

        updateConfig: function(config) {
            this.enableView.updateConfig(config.pollForUpdates);

            this.$pollingInterval.val(config.pollingInterval);
        }
    });
});
