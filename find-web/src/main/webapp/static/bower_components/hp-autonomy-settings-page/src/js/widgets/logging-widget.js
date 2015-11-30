/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module settings/js/widgets/logging-widget
 */
define([
    'settings/js/widget',
    'settings/js/controls/enable-view',
    'text!settings/templates/widgets/logging-widget.html'
], function(Widget, EnableView, template) {

    var templateFunction = _.template(template);

    /**
     * @typedef LoggingWidgetOptions
     * @desc Extends WidgetOptions
     * @property {string} testURL Url called to test configuration
     *
     */
    /**
     * @typedef LoggingWidgetStrings
     * @desc Extends WidgetStrings
     * @property {string} compression Label for the compression dropdown
     * @property {string} daily Label for daily rollover option
     * @property {string} gzip Label for the gzip compression option
     * @property {string} invalidMaxHistory Message displayed when the max history option is invalid
     * @property {string} invalidMaxSize Message displayed when the maximum file size option is invalid
     * @property {string} invalidSyslogServer Message displayed when the syslog server configuration is invalid
     * @property {string} logFile Label for the log file section
     * @property {string} logFileToggle Label for the button that enables file logging
     * @property {string} maxHistory Label for the max history option
     * @property {string} maxSize Label for maximum file size option
     * @property {string} monthly Label for monthly rollover option
     * @property {string} none Label for the no compression option
     * @property {string} rolloverFrequency Label for the rollover frequency options
     * @property {string} syslog Label for the syslog section
     * @property {string} syslogHostPlaceholder Placeholder for the syslog server host
     * @property {string} syslogPortPlaceholder Placeholder for the syslog server port
     * @property {string} syslogToggle Label for the button that enables syslog logging
     * @property {string} testButton Label for the test syslog settings button
     * @property {string} zip Label for the zip compression option
     *
     */
    /**
     * @name module:settings/js/widgets/logging-widget.LoggingWidget
     * @desc Widget for configuring logging services
     * @constructor
     * @param {LoggingWidgetOptions} options Options for the widget
     * @extends module:settings/js/widget.Widget
     */
    return Widget.extend(/** @lends module:settings/js/widgets/logging-widget.LoggingWidget.prototype */ {
        events: _.extend(Widget.prototype.events, {
            'click [name="test-logging"]': function() {
                if (!this.testRequest && this.validateSyslogInputs()) {
                    this.testRequest = $.ajax({
                        contentType: 'application/json',
                        data: JSON.stringify(this.getConfig().syslog),
                        dataType: 'json',
                        type: 'POST',
                        url: this.testURL,
                        complete: _.bind(function() {
                            this.testRequest = null;
                            this.updateTestSyslogButton();
                        }, this),
                        error: _.bind(function() {
                            this.$testResponse.text(this.strings.testFailure).removeClass('hide');
                        }, this),
                        success: _.bind(function(response) {
                            this.$testResponse.text(this.strings.testSuccess(response.message)).removeClass('hide');
                        }, this)
                    });

                    this.updateTestSyslogButton();
                }
            }
        }),

        initialize: function(options) {
            Widget.prototype.initialize.apply(this, arguments);
            this.testURL = options.testURL;

            this.logFileToggle = new EnableView({
                enableIcon: 'icon-file-text',
                strings: this.strings.logFileToggle
            });

            this.syslogToggle = new EnableView({
                enableIcon: 'icon-' + this.strings.iconClass,
                strings: this.strings.syslogToggle
            });
        },

        /**
         * @desc Renders the widget
         */
        render: function() {
            this.stopListening();
            Widget.prototype.render.apply(this, arguments);

            this.$content.append(templateFunction({strings: this.strings}));
            this.$compression = this.$('[name="file-compression"]');
            this.$frequency = this.$('[name="rollover-frequency"]');
            this.$maxHistory = this.$('[name="max-history"]');
            this.$maxSize = this.$('[name="max-size"]');
            this.$maxSizeUnit = this.$('[name="max-size-unit"]');
            this.$syslogHost = this.$('[name="syslog-host"]');
            this.$syslogPort = this.$('[name="syslog-port"]');
            this.$testButton = this.$('[name="test-logging"]');
            this.$testResponse = this.$('.logging-test-response');

            this.logFileToggle.setElement(this.$('.logfile-toggle')).render();
            this.syslogToggle.setElement(this.$('.syslog-toggle')).render();

            this.listenTo(this.logFileToggle, 'change', function(enabled) {
                this.logFileToggle.$el.closest('.settings-logging-section').find('.logging-control').prop('disabled', !enabled);
            });

            this.listenTo(this.syslogToggle, 'change', function(enabled) {
                if (!enabled && this.testRequest) {
                    this.testRequest.abort();
                    this.testRequest = null;
                }

                this.$testResponse.addClass('hide');
                this.syslogToggle.$el.closest('.settings-logging-section').find('.logging-control').prop('disabled', !enabled);
                this.updateTestSyslogButton();
            });

            this.updateTestSyslogButton();
        },


        /**
         * @typedef LogFileConfiguration
         * @property {string} compression The compression setting for the log file. Can be either NONE, ZIP or GZIP.
         * @property {boolean} enabled True if log file logging is enabled; false otherwise
         * @property {number} maxHistory The maximum number of files that will be kept
         * @property {number} maxSize The maximum size of a log file
         * @property {string} rolloverFrequency How often a log file is rotated. Can be DAILY or MONTHLY
         */
        /**
         * @typedef SyslogConfiguration
         * @property {boolean} enabled True if syslog server logging is enabled; false otherwise
         * @property {string} host The host of the syslog server
         * @property {number} port The port of the syslog server
         */
        /**
         * @typedef LoggingConfiguration
         * @property {LogFileConfiguration} logFile Log file configuration
         * @property {SyslogConfiguration} syslog Syslog server configuration
         */
        /**
         * @desc Returns the configuration for the wizard
         * @returns {LoggingConfiguration}
         */
        getConfig: function() {
            var maxSize = Math.floor(Number(this.$maxSize.val())) * Number(this.$maxSizeUnit.val());

            //noinspection JSValidateTypes
            return {
                logFile: {
                    compression: this.$compression.val(),
                    enabled: this.logFileToggle.getConfig(),
                    maxHistory: Math.floor(Number(this.$maxHistory.val())),
                    maxSize: maxSize,
                    rolloverFrequency: this.$frequency.val()
                },
                syslog: {
                    enabled: this.syslogToggle.getConfig(),
                    host: this.$syslogHost.val(),
                    port: Math.floor(Number(this.$syslogPort.val()))
                }
            };
        },

        /**
         * @desc Updates the state of the test syslog button
         * @protected
         */
        updateTestSyslogButton: function() {
            var $i = this.$testButton.find('i').removeClass();

            if (this.syslogToggle.getConfig()) {
                if (this.testRequest) {
                    this.$testButton.prop('disabled', true);
                    $i.addClass('icon-spin icon-refresh');
                } else {
                    this.$testButton.prop('disabled', false);
                    $i.addClass('icon-ok');
                }
            } else {
                this.$testButton.prop('disabled', true);
                $i.addClass('icon-ok');
            }
        },

        /**
         * @desc Updates the widget with the given configuration
         * @param {LoggingConfiguration} config The new configuration for the widget
         */
        updateConfig: function(config) {
            Widget.prototype.updateConfig.apply(this, arguments);

            var maxSize = config.logFile.maxSize / 1024;

            if (maxSize < 1) {
                maxSize = 1;
            }

            var coefficient = 1024;

            _.each(['MB', 'GB'], function() {
                if (maxSize >= 1024) {
                    maxSize /= 1024;
                    coefficient *= 1024;
                }
            });

            this.$compression.val(config.logFile.compression);
            this.$frequency.val(config.logFile.rolloverFrequency);
            this.$maxHistory.val(config.logFile.maxHistory);
            this.$maxSize.val(maxSize);
            this.$maxSizeUnit.val(coefficient);
            this.logFileToggle.updateConfig(config.logFile.enabled);

            this.$syslogHost.val(config.syslog.host);
            this.$syslogPort.val(config.syslog.port);
            this.syslogToggle.updateConfig(config.syslog.enabled);

            this.updateTestSyslogButton();
        },

        /**
         * @desc Validates inputs and applies formatting accordingly
         * @returns {boolean} False if max history is not an integer in 1-99999 or max file size is not a positive
         * integer, or the syslog configuration is invalid; true otherwise
         */
        validateInputs: function() {
            var isValid = true;

            if (this.logFileToggle.getConfig()) {
                var maxHistory = Number(this.$maxHistory.val());

                if (_.isNaN(maxHistory) || maxHistory % 1 !== 0 || maxHistory < 0 || maxHistory > 99999) {
                    this.updateInputValidation(this.$maxHistory, false);
                    isValid = false;
                }

                var maxSize = Number(this.$maxSize.val()) * Number(this.$maxSizeUnit.val());

                if (_.isNaN(maxSize) || maxSize % 1 !== 0 || maxSize < 0) {
                    this.updateInputValidation(this.$maxSize, false);
                    isValid = false;
                }
            }

            // Must be in this order or we won't see syslog validation messages if the log file section fails
            return this.validateSyslogInputs() && isValid;
        },

        /**
         * @desc Validates the syslog inputs
         * @returns {boolean} False if the syslog host is empty or the port is not an integer in 1-65535; true otherwise
         * @protected
         */
        validateSyslogInputs: function() {
            var isValid = true;

            if (this.syslogToggle.getConfig()) {
                if ($.trim(this.$syslogHost.val()) === '') {
                    this.updateInputValidation(this.$syslogHost);
                    isValid = false;
                }

                var port = Number(this.$syslogPort.val());

                if (_.isNaN(port) || port % 1 !== 0 || port <= 0 || port >= 65536) {
                    this.updateInputValidation(this.$syslogPort);
                    isValid = false;
                }
            }

            return isValid;
        }
    });

});