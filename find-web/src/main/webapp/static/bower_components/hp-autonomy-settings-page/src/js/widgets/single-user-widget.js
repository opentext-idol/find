/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module settings/js/widgets/single-user-widget
 */
define([
    'settings/js/widget',
    'settings/js/controls/password-view',
    'text!settings/templates/widgets/single-user-widget.html'
], function(Widget, PasswordView, template) {

    template = _.template(template);

    /**
     * @typedef SingleUserWidgetStrings
     * @desc Extends WidgetStrings
     * @property {string} confirmPassword Label for the confirm password input
     * @property {string} currentPassword Label for the current password input
     * @property {string} newPassword Label for the new password input
     * @property {string} passwordMismatch Message displayed when the new password and the confirm password do not match
     * @property {string} passwordRedacted Placeholder for redacted passwords
     * @property {string} username Label for the confirm username input
     * @property {string} validateConfirmPasswordBlank Message displayed when the confirm password input is empty
     * @property {string} validateCurrentPasswordBlank Message displayed when the current password input is empty
     * @property {string} validateNewPasswordBlank Message displayed when the new password input is empty
     * @property {string} validateUsernameBlank Message displayed when the username input is empty
     */
    /**
     * @typedef SingleUserWidgetOptions
     * @desc Extends WidgetOptions
     * @property {SingleUserWidgetStrings} strings Strings for the widget
     */
    /**
     * @name module:settings/js/widgets/single-user-widget.SingleUserWidget
     * @desc Widget for configuring a single user for an application (typically an Administrator user)
     * @constructor
     * @param {SingleUserWidgetOptions} options Options for the widget
     * @extends module:settings/js/widget.Widget
     */
    return Widget.extend(/** @lends module:settings/js/widgets/single-user-widget.SingleUserWidget.prototype */{

        className: Widget.prototype.className + ' form-horizontal',

        initialize: function() {
            Widget.prototype.initialize.apply(this, arguments);

            this.currentPassword = new PasswordView({
                strings: {
                    passwordLabel: this.strings.currentPassword,
                    passwordRedacted: this.strings.passwordRedacted,
                    validatePasswordBlank: this.strings.validateCurrentPasswordBlank
                }
            });

            this.newPassword = new PasswordView({
                strings: {
                    passwordLabel: this.strings.newPassword,
                    passwordRedacted: this.strings.passwordRedacted,
                    validatePasswordBlank: this.strings.validateNewPasswordBlank
                }
            });

            this.confirmPassword = new PasswordView({
                strings: {
                    passwordLabel: this.strings.confirmPassword,
                    passwordRedacted: this.strings.passwordRedacted,
                    validatePasswordBlank: this.strings.validateConfirmPasswordBlank
                }
            });

            this.passwordViews = [
                this.currentPassword,
                this.newPassword,
                this.confirmPassword
            ];
        },

        /**
         * @desc Renders the widget
         */
        render: function() {
            Widget.prototype.render.call(this);

            this.$content.append(template({
                strings: this.strings
            }));

            _.each(this.passwordViews, function(view) {
                view.render();

                this.$('.passwords').append(view.el);
            }, this);

            this.$('.settings-label').addClass('single-user-settings-label');

            this.$username = this.$('input[name="username"]');
        },

        /**
         * @typedef SingleUser
         * @property {string} currentPassword The single user's current password
         * @property {string} passwordRedacted True if all the passwords are redacted; false otherwise
         * @property {string} plaintextPassword The single user's new password
         * @property {string} username The single user's username
         */
        /**
         * @typedef SingleUserConfig
         * @property {string} method The method used for authentication. Set to 'singleUser'
         * @property {SingleUser} singleUser The single user configuration
         */
        /**
         * @desc Returns the configuration associated with the widget
         * @returns {SingleUserConfig}
         */
        getConfig: function() {
            var passwordRedacted = _.every(this.passwordViews, function(view) {
                return view.getConfig().passwordRedacted
            });

            //noinspection JSValidateTypes
            return {
                method: 'singleUser',
                singleUser: {
                    currentPassword: this.currentPassword.getConfig().password,
                    passwordRedacted: passwordRedacted,
                    plaintextPassword: this.newPassword.getConfig().password,
                    username: this.$username.val()
                }
            }
        },

        /**
         * @desc Updates the widget with the given configuration
         * @param {SingleUserConfig} config The new configuration for the widget
         */
        updateConfig: function(config) {
            Widget.prototype.updateConfig.apply(this, arguments);

            var singleUser = config.singleUser;

            this.$username.val(singleUser.username);

            _.invoke(this.passwordViews, 'updateConfig', {password: null, passwordRedacted: singleUser.passwordRedacted});
        },

        /**
         * @desc Validates the widget and applies formatting accordingly
         * @returns {boolean} False if any of the following:
         * <ul>
         * <li>The username is empty
         * <li>Any of the password fields is empty and not redacted
         * <li>Any of the password fields is not redacted and the new password and the confirm password do not match
         * </ul>
         * or true otherwise
         */
        validateInputs: function() {
            var isValid = true;

            if(!this.$username.val()) {
                isValid = false;

                this.updateInputValidation(this.$username, false);
            }

            _.each(this.passwordViews, function(view) {
                // validate inputs first for better error handling
                isValid = view.validateInputs() && isValid;
            });

            var currentPasswordConfig = this.currentPassword.getConfig();
            var newPasswordConfig = this.newPassword.getConfig();
            var confirmPasswordConfig = this.confirmPassword.getConfig();

            // if any password has been modified, inequality means invalidation
            if((!currentPasswordConfig.isRedacted || !newPasswordConfig.isRedacted || !confirmPasswordConfig.isRedacted) &&
                    newPasswordConfig.password !== confirmPasswordConfig.password) {
                isValid = false;
                this.$('.password-mismatch').removeClass('hide');
            }

            return isValid;
        }

    });

});