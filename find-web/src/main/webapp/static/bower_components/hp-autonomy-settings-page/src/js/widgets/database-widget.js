/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module settings/js/widgets/database-widget
 */
define([
    'settings/js/server-widget',
    'settings/js/controls/password-view',
    'settings/js/controls/enable-view',
    'text!settings/templates/widgets/database-widget.html'
], function(ServerWidget, PasswordView, EnableView, template) {
    /**
     * @typedef DatabaseWidgetStrings
     * @desc Includes all properties from ServerWidgetStrings, EnableViewStrings, and PasswordViewStrings
     * @property {string} databaseCheckbox Label for the database checkbox
     * @property {string} databaseLabel Label for the database field
     * @property {string} flywayMigrationFromEmpty String to display if the database will need to be set up for the
     * application
     * @property {string} flywayMigrationUpgrade String to display if the database schema needs to be upgraded
     * @property {string} hostPlaceholder Placeholder for the host input
     * @property {string} portPlaceholder Placeholder for the port input
     * @property {string} usernameLabel Label for the username input
     * @property {string} validateDatabaseBlank Message to display if the database is blank
     * @property {string} validateHostBlank Message to display if the host is blank
     * @property {string} validateUsernameBlank Message to display if the username is blank
     */
    /**
     * @typedef DatabaseWidgetOptions
     * @desc Extends ServerWidgetOptions
     * @property {boolean} canDisable True if the database can be disabled; false otherwise
     * @property {DatabaseWidgetStrings} strings Strings for the widget
     */
    /**
     * @name module:settings/js/widgets/database-widget.DatabaseWidget
     * @desc Widget for configuring a relational database
     * @constructor
     * @param {DatabaseWidgetOptions} options Options for the widget
     * @extends module:settings/js/server-widget.ServerWidget
     */
    return ServerWidget.extend(/** @lends module:settings/js/widgets/database-widget.DatabaseWidget.prototype */{
        className: ServerWidget.prototype.className,

        /**
         * @typedef DatabaseWidgetTemplateParameters
         * @property {DatabaseWidgetStrings} strings Strings for the template
         */
        /**
         * @callback module:settings/js/widgets/database-widget.DatabaseWidget~DatabaseTemplate
         * @param {DatabaseWidgetTemplateParameters} parameters
         */
        /**
         * @desc Base template for the widget. Override if using Bootstrap 3
         * @type module:settings/js/widgets/database-widget.DatabaseWidget~DatabaseTemplate
         */
        databaseTemplate: _.template(template),

        formControlClass: '',

        initialize: function(options) {
            ServerWidget.prototype.initialize.call(this, options);
            this.passwordView = new PasswordView({
                strings: this.strings,
                className: this.controlGroupClass,
                formControlClass: this.formControlClass
            });

            if (options.canDisable) {
                this.enableView = new EnableView({enableIcon: 'icon-file', strings: this.strings});
            }
        },

        /**
         * @desc Renders the widget
         */
        render: function() {
            ServerWidget.prototype.render.call(this);

            var $validateButtonParent = this.$('button[name=validate]').parent();
            $validateButtonParent.before(this.databaseTemplate({
                strings: this.strings
            }));

            this.$database = this.$('input[name=database]');
            this.$databaseCheckbox = this.$('input[type="checkbox"]');
            this.$host = this.$('input[name=host]');
            this.$port = this.$('input[name=port]');
            this.$username = this.$('input[name=username]');
            this.$protocol = this.$('.protocol');

            this.passwordView.render();
            this.$databaseCheckbox.parent().before(this.passwordView.$el);

            if (this.enableView) {
                this.enableView.render();
                $validateButtonParent.before(this.enableView.$el);
            }
        },

        /**
         * @typedef DatabaseConfiguration
         * @property {string} database The name of the database
         * @property {boolean} [enabled] True if the database is enabled; false otherwise
         * @property {string} host The host the database is located on
         * @property {string} password The password. If passwordRedacted is true then this should be empty
         * @property {boolean} passwordRedacted True if the password exists but is not visible; false otherwise
         * @property {number} port The port the database uses to communicate
         * @property {string} protocol The database protocol used to connect to the database (postgresql, mysql etc.)
         * @property {string} username The user that will connect to database
         */
        /**
         * @desc Returns the configuration for the widget
         * @returns {DatabaseConfiguration} The configuration for the widget
         */
        getConfig: function() {
            var config = _.extend({
                protocol: this.protocol,
                database: this.$databaseCheckbox.prop('checked') ? this.$username.val() : this.$database.val(),
                host: this.$host.val(),
                port: Number(this.$port.val()),
                username: this.$username.val()
            }, this.passwordView.getConfig());

            if (this.enableView) {
                _.extend(config, {
                    enabled: this.enableView.getConfig()
                });
            }

            //noinspection JSValidateTypes
            return config;
        },

        /**
         * @desc Handles input validation. Checking the database checkbox disables the database input. If the database
         * checkbox is checked, sets value of the database input to the value of the username input
         * @param {Event} evt jQuery event
         */
        handleInputChange: function(evt) {
            ServerWidget.prototype.handleInputChange.apply(this, arguments);
            var $input = $(evt.target);

            if ($input.is(this.$databaseCheckbox)) {
                this.$database.prop('disabled', this.$databaseCheckbox.prop('checked'));
            }

            if (this.$databaseCheckbox.prop('checked') && ($input.is(this.$databaseCheckbox) || $input.is(this.$username))) {
                this.$database.val(this.$username.val());
            }
        },

        /**
         * @typedef DatabaseValidationResponse
         * @property {string} data.version String defining the version of the migration
         */
        /**
         * @desc Returns a message stating validation was successful. If response.data.sourceVersion === '0', returns
         * this.strings.flywayMigrationFromEmpty. If response.data is defined, returns
         * this.strings.flywayMigrationUpgrade. Otherwise returns this.strings.validateSuccess.
         * @param {DatabaseValidationResponse} response
         * @returns {string}
         */
        getValidationSuccessMessage: function(response) {
            // if we get back flyway migration data, use that in the message
            if (response.data) {
                if(response.data.sourceVersion === '0') {
                    return this.strings.flywayMigrationFromEmpty;
                }
                else {
                    return this.strings.flywayMigrationUpgrade;
                }
            }
            else {
                return ServerWidget.prototype.getValidationSuccessMessage.call(this, response);
            }
        },

        /**
         * @returns {boolean} True if the database is enabled or cannot be disabled; false otherwise
         */
        shouldValidate: function() {
            if (this.enableView) {
                return this.enableView.getConfig();
            }

            return true;
        },

        /**
         * @desc Updates the widget with new configuration
         * @param {DatabaseConfiguration} config The new config for the wizard
         */
        updateConfig: function(config) {
            ServerWidget.prototype.updateConfig.apply(this, arguments);

            this.protocol = config.protocol;
            this.$protocol.text(this.protocol);
            this.$database.val(config.database);
            this.$host.val(config.host);
            this.$port.val(config.port);
            this.$username.val(config.username);
            this.passwordView.updateConfig(config);

            if (this.enableView) {
                this.enableView.updateConfig(config.enabled);
            }

            if (config.database === '' && config.username !== '') {
                this.$database.val(config.username);
            }

            var checkboxState = this.$database.val() === this.$username.val();
            this.$databaseCheckbox.prop('checked', checkboxState);
            this.$database.prop('disabled', checkboxState);
        },

        /**
         * @desc Validates the widget and applies formatting appropriately
         * @returns {boolean} False if any of host, username and database are blank or the password is invalid; true
         * otherwise
         */
        validateInputs: function() {
            var isValid = true;

            if (this.shouldValidate()) {
                var config = this.getConfig();

                if (config.host === '') {
                    isValid = false;
                    this.updateInputValidation(this.$host, false);
                }

                if (config.username === '') {
                    isValid = false;
                    this.updateInputValidation(this.$username, false);
                }

                if (config.database === '') {
                    isValid = false;
                    this.updateInputValidation(this.$database, false);
                }

                // needs to be this way round to apply formatting
                isValid = this.passwordView.validateInputs() && isValid;
            }

            return isValid;
        }
    });

});
