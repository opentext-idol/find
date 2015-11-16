/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module settings/js/controls/password-view
 */
define([
    '../../../../backbone/backbone',
    'text!settings/templates/controls/password-view.html'
], function(Backbone, template) {

    template = _.template(template);

    /**
     * @typedef PasswordViewConfig
     * @property {string} password The password. If passwordRedacted is true then this should be empty
     * @property {boolean} passwordRedacted True if the password exists but is not visible; false otherwise
     */
    /**
     * @typedef PasswordViewStrings
     * @property {string} passwordDescription Description for password field
     * @property {string} passwordLabel Label for password field
     * @property {string} passwordRedacted Placeholder for password fields where a password exists
     * @property {string} validatePasswordBlank String which displays if the given password is blank
     */
    /**
     * @typedef PasswordViewOptions
     * @property {boolean} [enabled=true] True if the view should be enabled; false otherwise
     * @property {string} [formControlClass=''] Class applied to form inputs. Set to form-control if using Bootstrap 3
     * @property {PasswordViewStrings} strings
     */
    /**
     * @name module:settings/js/controls/password-view.PasswordView
     * @desc Utility control for widgets which require passwords
     * @param {PasswordViewOptions} options Options for the view
     * @constructor
     * @extends Backbone.View
     */
    return Backbone.View.extend(/** @lends module:settings/js/controls/password-view.PasswordView.prototype*/ {
        /**
         * @desc Class applied to the view. Override if using Bootstrap 3
         * @default control-group
         */
        className: 'control-group',

        events: {
            'change input': 'handleInputChange'
        },

        initialize: function(options) {
            _.bindAll(this, 'getConfig', 'updateConfig');
            this.enabled = !_.isUndefined(options.enabled) ? options.enabled : true;
            this.strings = options.strings;
            this.formControlClass = options.formControlClass || '';
        },

        /**
         * @desc Renders the view
         */
        render: function() {
            this.$el.html(template({
                enabled: this.enabled,
                strings: this.strings,
                formControlClass: this.formControlClass
            }));

            this.$input = this.$('input[name="password"]').prop('disabled', !this.enabled);
            this.$required = this.$('.settings-required-flag').toggleClass('hide', !this.enabled);
        },

        /**
         * @desc Return the current state of the view
         * @returns {PasswordViewConfig} The current state of the view
         */
        getConfig: function() {
            //noinspection JSValidateTypes
            return {
                password: this.$input.val(),
                passwordRedacted: this.isRedacted
            };
        },

        /**
         * @desc Handler called when an input changes. Removes validation formatting and error messages
         */
        handleInputChange: function() {
            this.$el.removeClass('success, error');
            this.$('.settings-client-validation').addClass('hide');
        },

        /**
         * @desc Enables or disables the view
         * @param {boolean} state True if the view should be enabled; false otherwise
         */
        setEnabled: function(state) {
            this.enabled = state;
            this.$input.prop('disabled', !state);
            this.$required.toggleClass('hide', !state);
        },

        /**
         * @desc Updates the current state of the view
         * @param {PasswordViewConfig} config The new config
         */
        updateConfig: function(config) {
            this.$input.val(config.password);
            this.isRedacted = config.passwordRedacted;

            if (this.isRedacted) {
                this.$input.on('change keyup', _.bind(function onPasswordChange(evt) {
                    if (evt.type === 'keyup' && this.redactedPassword && this.redactedPassword === this.$input.val()) {
                        return;
                    }

                    this.isRedacted = false;

                    this.$input.attr('placeholder', '').removeClass('placeholder')
                        .off('change keyup', onPasswordChange);
                }, this));

                if ('placeholder' in this.$input[0]) {
                    this.$input.attr('placeholder', this.strings.passwordRedacted);
                }
                else {
                    // browser doesn't support placeholders, e.g. IE <= 9
                    this.$input.val(this.strings.passwordRedacted).addClass('placeholder');
                }

                this.redactedPassword = this.$input.val();
            }
        },

        /**
         * @desc Validates the inputs and displays error formatting if invalid
         * @returns {boolean} True if the view is enabled, redacted, or the password is not empty; false otherwise
         */
        validateInputs: function() {
            if (this.enabled && !this.isRedacted && this.$input.val() === '') {
                this.$el.addClass('error');
                this.$('.settings-client-validation').removeClass('hide');
                return false;
            }

            return true;
        }
    });

});
