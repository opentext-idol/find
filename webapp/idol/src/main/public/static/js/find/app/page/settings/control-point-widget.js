/*
 * Copyright 2020 Open Text.
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
    'settings/js/widgets/aci-widget',
    'find/app/page/settings/enable-view',
    'settings/js/controls/password-view',
    'text!find/templates/app/page/settings/widget.html',
    'text!find/templates/app/page/settings/server-widget.html',
    'text!find/templates/app/page/settings/aci-widget.html',
    'text!find/templates/app/page/settings/control-point-widget.html',
], function (
    AciWidget, EnableView, PasswordView,
    widgetTemplate, serverTemplate, aciTemplate, controlPointTemplate
) {

    return AciWidget.extend({
        className: 'panel-group',
        controlGroupClass: 'form-group',
        formControlClass: 'form-control',
        errorClass: 'has-error',
        successClass: 'has-success',

        widgetTemplate: _.template(widgetTemplate),
        serverTemplate: _.template(serverTemplate),
        aciTemplate: _.template(aciTemplate),
        controlPointTemplate: _.template(controlPointTemplate),

        initialize: function () {
            AciWidget.prototype.initialize.apply(this, arguments);

            this.enableView = new EnableView({
                enableIcon: 'fa fa-file',
                strings: this.strings
            });
            this.listenTo(this.enableView, 'change', function() {
                this.$('.settings-required-flag').toggleClass('hide', !this.enableView.getConfig());
            });

            // credentials not needed for now
            // this.passwordView = new PasswordView({
            //     strings: this.strings,
            //     className: this.controlGroupClass + ' m-t-sm',
            //     formControlClass: this.formControlClass
            // });
            this.credentials = null;
        },

        render: function () {
            AciWidget.prototype.render.apply(this, arguments);

            const $testControl = this.$('button[name=validate]').parent();
            this.enableView.render();
            $testControl.before(this.enableView.el);
            // credentials not needed for now
            // $testControl.before(this.controlPointTemplate({
            //     strings: this.strings
            // }));
            // this.$username = this.$('input[name=username]');
            // this.passwordView.render();
            // $testControl.before(this.passwordView.$el);
        },

        updateConfig: function (config) {
            AciWidget.prototype.updateConfig.call(this, config.server);
            this.enableView.updateConfig(config.enabled);
            // credentials not needed for now
            // this.$username.val(config.server.credentials.username);
            // this.passwordView.updateConfig({
            //     passwordRedacted: true,
            //     password: ''
            // });
            this.credentials = config.server.credentials;
        },

        getConfig: function () {
            const aciServer = AciWidget.prototype.getConfig.call(this);
            return {
                enabled: this.enableView.getConfig(),
                server: {
                    protocol: aciServer.protocol,
                    host: aciServer.host,
                    port: aciServer.port,
                    // credentials not needed for now
                    // credentials: {
                    //     username: this.$username.val(),
                    //     password: this.passwordView.getConfig().password // empty if unchanged
                    // }
                    credentials: this.credentials
                }
            };
        },

        shouldValidate: function () {
            return this.enableView.getConfig();
        },

        validateInputs: function () {
            if (!this.shouldValidate()) {
                return true;
            }

            if (!AciWidget.prototype.validateInputs.call(this)) {
                return false;
            }

            // credentials not needed for now
            // if (this.$username.val() === '') {
            //     this.updateInputValidation(this.$username, false);
            //     return false;
            // }
            //
            // return this.passwordView.validateInputs();
            return true;
        }

    });

});
