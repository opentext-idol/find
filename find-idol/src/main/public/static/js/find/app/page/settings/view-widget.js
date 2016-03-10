/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/settings/aci-widget',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/settings/view-widget.html'
], function(AciWidget, i18n, template) {

    return AciWidget.extend({
        viewTemplate: _.template(template),

        render: function() {
            AciWidget.prototype.render.call(this);

            this.$('button[name="validate"]').parent().before(this.viewTemplate({
                strings: this.strings
            }));

            this.$('.connector-container').append(this.aciTemplate({
                strings: this.strings
            }));

            this.$modeSelect = this.$('.viewing-mode-input');

            this.$referenceField = this.$('[name="referenceField"]');

            // make sure we don't get the ones for connectors
            this.$host = this.$('input[name=host]').eq(0);
            this.$port = this.$('input[name=port]').eq(0);
            this.$protocol = this.$('select[name=protocol]').eq(0);

            this.$connectorHost = this.$('.connector-container input[name=host]');
            this.$connectorPort = this.$('.connector-container input[name=port]');
            this.$connectorProtocol = this.$('.connector-container select[name=protocol]');

            var toggleInputs = _.bind(function () {
                this.$('.connector-container').toggle(this.$modeSelect.val() === 'CONNECTOR');
                this.$('.field-form-group').toggle(this.$modeSelect.val() === 'FIELD');
            }, this);

            this.$modeSelect.on('input', toggleInputs);

            toggleInputs();
        },

        updateConfig: function(config) {
            AciWidget.prototype.updateConfig.apply(this, arguments);

            this.$modeSelect.val(config.viewingMode);
            this.$referenceField.val(config.referenceField);

            this.$connectorHost.val(config.connector.host);
            this.$connectorPort.val(config.connector.port);
            this.$connectorProtocol.val(config.connector.protocol);

            this.productTypeRegex = config.connector.productTypeRegex;
        },

        getConfig: function() {
            var config = AciWidget.prototype.getConfig.call(this);

            return _.extend({
                referenceField: this.$referenceField.val(),
                viewingMode: this.$modeSelect.val(),
                connector: {
                    host: this.$connectorHost.val(),
                    port: this.$connectorPort.val(),
                    productTypeRegex: this.productTypeRegex,
                    protocol: this.$connectorProtocol.val()
                }
            }, config);
        },

        handleValidation: function(config, response) {
            if (_.isEqual(config, this.lastValidationConfig)) {
                if (response.data && response.data.validation === 'CONNECTOR_VALIDATION_ERROR') {
                    this.updateInputValidation(this.$('.connector-container .form-group'), false, this.getValidationFailureMessage(response.data.connectorValidation))
                }
                else if(response.data === "REFERENCE_FIELD_BLANK") {
                    this.updateInputValidation(this.$referenceField, false, this.strings.referenceFieldBlank);
                }
                else {
                    AciWidget.prototype.handleValidation.apply(this, arguments);
                }
            }
        },

        validateInputs: function() {
            var isValid = AciWidget.prototype.validateInputs.apply(this, arguments);

            if (isValid) {
                var mode = this.$modeSelect.val();

                if (mode === 'FIELD') {
                    if (this.$referenceField.val() === '') {
                        this.updateInputValidation(this.$referenceField, false);
                        return false;
                    }
                }
                else if (mode === 'CONNECTOR') {
                    if (this.$connectorHost.val() === '') {
                        this.updateInputValidation(this.$connectorHost, false, this.strings.validateHostBlank);
                        return false;
                    }

                    var port = Number(this.$connectorPort.val());

                    if (port <= 0 || port > 65535) {
                        this.updateInputValidation(this.$connectorPort, false, this.strings.validatePortInvalid);
                        return false;
                    }

                    return true;
                }
            }

            return isValid;
        }
    });

});