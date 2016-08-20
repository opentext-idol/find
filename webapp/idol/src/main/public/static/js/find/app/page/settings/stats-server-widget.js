/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/settings/aci-widget',
    'find/app/page/settings/enable-view',
    'i18n!find/nls/bundle',
    'underscore'
], function(AciWidget, EnableView, i18n, _) {

    var getMessageFromKey = function(key) {
        if (_.isString(key)) {
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
            AciWidget.prototype.render.apply(this, arguments);

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