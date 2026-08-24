const _ = require('underscore');
const AciWidget = require('find/app/page/settings/aci-widget');
const EnableView = require('find/app/page/settings/enable-view');
const i18n = require('find/nls/bundle');

/**
 * Like `AciWidget`, but can be enabled and disabled.  Config has `enabled` and `server`
 * properties.  Requires strings required by `EnableView`.
 */

module.exports = AciWidget.extend({
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

