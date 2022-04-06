/*
 * (c) Copyright 2014-2017 Micro Focus or one of its affiliates.
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
    'find/app/page/settings/aci-widget',
    'find/app/page/settings/enable-view',
    'i18n!find/nls/bundle'
], function(_, AciWidget, EnableView, i18n) {
    'use strict';

    var getMessageFromKey = function(key) {
        if(_.isString(key)) {
            return i18n['settings.statsserver.validation.' + key];
        }

        return null;
    };

    return AciWidget.extend({
        initialize: function(options) {
            AciWidget.prototype.initialize.apply(this, arguments);

            this.enableView = new EnableView({
                enableIcon: 'fa fa-file',
                strings: this.strings
            })
        },

        render: function() {
            AciWidget.prototype.render.apply(this);

            this.enableView.render();
            var $validateButtonParent = this.$('button[name=validate]').parent();
            $validateButtonParent.before(this.enableView.el);

            this.listenTo(this.enableView, 'change', function() {
                this.$('.settings-required-flag').toggleClass('hide', !this.enableView.getConfig());
            })
        },

        getConfig: function() {
            return {
                enabled: this.enableView.getConfig(),
                server: AciWidget.prototype.getConfig.apply(this, arguments)
            }
        },

        updateConfig: function(config) {
            this.enableView.updateConfig(config.enabled);
            AciWidget.prototype.updateConfig.call(this, config.server);
        },

        getValidationFailureMessage: function(response) {
            return getMessageFromKey(response.data) || AciWidget.prototype.getValidationFailureMessage.call(this, response);
        },

        getValidationSuccessMessage: function(response) {
            return getMessageFromKey(response.data) || AciWidget.prototype.getValidationSuccessMessage.call(this, response);
        }
    });
});
