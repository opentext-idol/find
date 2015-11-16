/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module settings/js/widgets/mail-widget
 */
define([
    'settings/js/server-widget',
    'settings/js/controls/password-view',
    'settings/js/controls/enable-view',
    'text!settings/templates/widgets/mail-widget.html'
], function(ServerWidget, PasswordView, EnableView, template) {

    var defaultPorts = {
        NONE: 25,
        STARTTLS: 587,
        TLS: 465
    };

    template = _.template(template);

    /**
     * @typedef MailWidgetStrings
     * @desc Extends WidgetStrings, EnableViewStrings and PasswordViewStrings
     * @property {string} hostPlaceholder Placeholder for the mail server host input
     * @property {string} portPlaceholder Placeholder for the mail server port input
     * @property {string} validateHostBlank Message displayed when the mail server host is empty
     * @property {string} connectionSecurity Label for the security method dropdown
     * @property {string} fromLabel Label for the sender input
     * @property {string} validateFromBlank Message displayed when the sender input is empty
     * @property {string} toLabel Label for the recipient input
     * @property {string} toDescription Instructions for the recipient input
     * @property {string} validateFromBlank Message displayed when the recipient input is empty
     * @property {string} mailCheckbox Label for checkbox which enables authentication
     * @property {string} usernameLabel Label for the username input
     * @property {string} validateUsernameBlank Message displayed when the username input is empty
     */
    /**
     * @typedef MailWidgetOptions
     * @desc Extends ServerWidgetOptions
     * @property {object.<string,string>} securityTypes Security options to use. Keys should be NONE, STARTTLS and TLS.
     * Values should be display names.
     * @property {MailWidgetStrings} strings Strings for the widget
     */
    /**
     * @name module:settings/js/widgets/mail-widget.MailWidget
     * @desc Widget for configuring mail server settings
     * @constructor
     * @param {MailWidgetOptions} options Options for the widget
     * @extends module:settings/js/server-widget.ServerWidget
     */
    return ServerWidget.extend(/** @lends module:settings/js/widgets/mail-widget.MailWidget.prototype */{
        events: _.extend({
            'change select[name=connection-security]': 'handleSecurityChange'
        }, ServerWidget.prototype.events),

        initialize: function(options) {
            ServerWidget.prototype.initialize.call(this, options);
            _.bindAll(this, 'handleSecurityChange', 'updateAuthState');

            this.passwordView = new PasswordView({
                enabled: false,
                strings: this.strings
            });

            this.enableView = new EnableView({
                enableIcon: 'icon-envelope',
                strings: this.strings
            });

            this.securityTypes = options.securityTypes;
        },

        /**
         * @desc Renders the widget
         */
        render: function() {
            ServerWidget.prototype.render.call(this);

            var $validateButtonParent = this.$('button[name=validate]').parent();

            $validateButtonParent.before(template({
                securityTypes: this.securityTypes,
                strings: this.strings
            }));

            this.$authCheckbox = this.$('input[type="checkbox"]');
            this.$from = this.$('input[name=from]');
            this.$connectionSecurity = this.$('select[name=connection-security]');
            this.$host = this.$('input[name=host]');
            this.$port = this.$('input[name=port]');
            this.$to = this.$('input[name=to]');
            this.$username = this.$('input[name=username]');

            this.passwordView.render();
            $validateButtonParent.before(this.passwordView.$el);
            this.enableView.render();
            $validateButtonParent.before(this.enableView.$el);
        },

        /**
         * @typedef MailConfiguration
         * @property {string} connectionSecurity The security method to use. Can be either NONE, TLS, or STARTTLS
         * @property {boolean} enabled True if the widget is enabled; false otherwise
         * @property {string} from The email address to be used as the sender
         * @property {string} host The host of the email server
         * @property {string} [password] The password to use when authenticating with the mail server. If
         * passwordRedacted is true then this should be empty
         * @property {boolean} [passwordRedacted] True if the password exists but is not visible; false otherwise
         * @property {number} port The port of the email server
         * @property {string} to Comma separated list of email addresses to be used as the recipient
         * @property {string} [username] The username to use when authenticating with the mail server
         */
        /**
         * @desc Returns the configuration represented by the widget
         * @returns {MailConfiguration} The configuration represented by the widget
         */
        getConfig: function() {
            var authenticationRequired = this.$authCheckbox.prop('checked');

            var baseConfig = {
                connectionSecurity: this.$connectionSecurity.val(),
                enabled: this.enableView.getConfig(),
                from: this.$from.val(),
                host: this.$host.val(),
                port: Number(this.$port.val()),
                to: _.compact(_.map(this.$to.val().split(','), function(val) {
                    return $.trim(val);
                }))
            };

            if (authenticationRequired) {
                //noinspection JSValidateTypes
                return _.extend(baseConfig, this.passwordView.getConfig(), {username: this.$username.val()});
            }
            else {
                //noinspection JSValidateTypes
                return baseConfig;
            }
        },

        handleInputChange: function(evt) {
            ServerWidget.prototype.handleInputChange.apply(this, arguments);
            var $input = $(evt.target);
            var authRequired = this.$authCheckbox.prop('checked');

            if ($input.is(this.$authCheckbox)) {
                this.updateAuthState(authRequired);
            }
        },

        /**
         * @desc Handler called when the security method is changed. Updates the port to the default for that type
         */
        handleSecurityChange: function() {
            var newSecurityType = this.$connectionSecurity.val();

            if (Number(this.$port.val()) === defaultPorts[this.lastSecurityType]) {
                this.$port.val(defaultPorts[newSecurityType]);
            }

            this.lastSecurityType = newSecurityType;
        },

        /**
         * @desc Enables the username and password inputs if authentication is required, and disables them if not
         * @param authRequired True if authentication should be used; false otherwise
         * @protected
         */
        updateAuthState: function(authRequired) {
            this.$authCheckbox.prop('checked', authRequired);
            this.$username.prop('disabled', !authRequired);
            this.$username.siblings('.settings-required-flag').toggleClass('hide', !authRequired);
            this.passwordView.setEnabled(authRequired);
        },

        /**
         * @desc Determines if the widget should be validated
         * @returns {boolean} True if the widget is enabled; false otherwise
         */
        shouldValidate: function() {
            return this.enableView.getConfig();
        },

        /**
         * @desc Updates the widget with the given config
         * @param {MailConfiguration} config The new configuration for the widget
         */
        updateConfig: function(config) {
            ServerWidget.prototype.updateConfig.apply(this, arguments);

            this.$connectionSecurity.val(config.connectionSecurity);
            this.$from.val(config.from);
            this.$host.val(config.host);
            this.$port.val(config.port);
            this.$to.val(config.to ? config.to.join(',') : '');
            this.$username.val(config.username);

            this.enableView.updateConfig(config.enabled);
            this.passwordView.updateConfig(config);
            this.lastSecurityType = config.connectionSecurity;
            this.updateAuthState(config.username !== '' || this.$authCheckbox.prop('checked'));
        },

        /**
         * @desc Validates the widget and applies formatting as appropriate
         * @returns {boolean} False if the host is empty, the from field is empty, the recipient list is empty, or the
         * username or password fields are blank if authentication is used; true otherwise
         */
        validateInputs: function() {
            var isValid = true;

            if (this.shouldValidate()) {
                var config = this.getConfig();

                if (config.host === '') {
                    isValid = false;
                    this.updateInputValidation(this.$host, false);
                }

                if (config.from === '') {
                    isValid = false;
                    this.updateInputValidation(this.$from, false);
                }

                if (!config.to.length) {
                    isValid = false;
                    this.updateInputValidation(this.$to, false);
                }

                if (this.$authCheckbox.prop('checked')) {
                    if (!this.passwordView.validateInputs()) {
                        isValid = false;
                    }

                    if (config.username === '') {
                        isValid = false;
                        this.updateInputValidation(this.$username, false);
                    }
                }
            }

            return isValid;
        }
    });

});
