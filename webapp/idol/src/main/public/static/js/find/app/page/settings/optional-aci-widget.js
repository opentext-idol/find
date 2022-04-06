define([
    'underscore',
    'find/app/page/settings/aci-widget',
    'find/app/page/settings/enable-view',
    'i18n!find/nls/bundle'
], function (_, AciWidget, EnableView, i18n) {
    'use strict';

    /**
     * Like `AciWidget`, but can be enabled and disabled.  Config has `enabled` and `server`
     * properties.  Requires strings required by `EnableView`.
     */
    return AciWidget.extend({
        initialize: function (options) {
            AciWidget.prototype.initialize.apply(this, arguments);

            this.enableView = new EnableView({
                enableIcon: 'fa fa-file',
                strings: this.strings
            })
        },

        render: function () {
            AciWidget.prototype.render.apply(this);

            this.enableView.render();
            this.$('button[name=validate]').parent().before(this.enableView.el);

            this.listenTo(this.enableView, 'change', function () {
                this.$('.settings-required-flag').toggleClass('hide', !this.enableView.getConfig());
            })
        },

        getConfig: function () {
            return {
                enabled: this.enableView.getConfig(),
                server: AciWidget.prototype.getConfig.apply(this, arguments)
            };
        },

        updateConfig: function (config) {
            this.enableView.updateConfig(config.enabled);
            AciWidget.prototype.updateConfig.call(this, config.server);
        }
    });

});
