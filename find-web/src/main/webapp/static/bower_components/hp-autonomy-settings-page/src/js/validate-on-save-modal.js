/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module settings/js/validate-on-save-modal
 */
define([
    '../../../backbone/backbone',
    'text!settings/templates/validate-on-save-modal.html',
    'text!settings/templates/validation-error-message.html'
], function(Backbone, template, errorTemplate) {

    /**
     * @typedef ValidateOnSaveModalStrings
     * @property {string} cancel Text for the cancel button
     * @property {string} close Text for close button that appears after saving
     * @property {string} confirm Description for the modal
     * @property {string} errorThrown Prefix for error messages which are the result of a server side exception
     * @property {string} retry Text for retry button, which appears if the save was unsuccessful
     * @property {string} save Text for save button
     * @property {string} saving Text that appears on the save button while saving is in progress
     * @property {string} success Status text indicating that the save was successful
     * @property {string} successMessage Message indicating that the save was successful
     * @property {string} title Title for the modal
     * @property {string} unknown Indicates that an error occurred, usually because the server did not return JSON
     */
    /**
     * @typedef ValidateOnSaveModalOptions
     * @property {object} config The config to validate and save
     * @property {ConfigModel} configModel The model containing the config
     * @property {function} success Callback called when validation is successful
     */
    /**
     * @name module:settings/js/validate-on-save-modal.ValidateOnSaveModal
     * @desc Modal for confirming the saving of configuration, which will validate the config before it saves. Renders
     * the modal.
     * @constructor
     * @param {ValidateOnSaveModalOptions} options
     */
    return Backbone.View.extend(/** @lends module:settings/js/validate-on-save-modal.ValidateOnSaveModal.prototype */{
        /**
         * @desc Classes initially applied to the modal. Override if using Bootstrap 3
         */
        className: 'modal hide fade',

        /**
         * @desc Default template. Override if using Bootstrap 3
         */
        template: _.template(template),

        events: {
            'click #settings-save-ok': 'handleOk'
        },

        initialize: function(options) {
            _.bindAll(this, 'handleError', 'handleOk', 'handleSuccess', 'remove');
            this.config = options.config;
            this.configModel = options.configModel;
            this.strings = options.strings;
            this.successCallback = options.success;

            this.successTemplate = _.template('<div class="alert alert-success"><strong><%-strings.success%></strong> <%-strings.successMessage%></strong></div>');

            this.errorTemplate = _.template(errorTemplate, undefined, {variable: 'ctx'});

            this.throbberTemplate = _.template('<i class="icon-spinner icon-spin icon-2x" style="vertical-align: middle;"></i> <strong style="vertical-align: middle;"><%-strings.saving%></strong>');

            this.render();
        },

        /**
         * @desc Renders the modal
         */
        render: function() {
            document.activeElement.blur();

            this.$el.html(this.template({strings: this.strings}))
                .modal({
                    backdrop: 'static',
                    keyboard: false
                })
                .on('hidden', this.remove);

            this.$cancel = this.$('button[data-dismiss="modal"]');
            this.$ok = this.$('#settings-save-ok');
            this.$body = this.$('.modal-body');
        },

        /**
         * @desc Handler called when saving the model fails. It will attempt to treat the responseText as JSON, and
         * check the validation property. If this exists, the cause of the validation failure will be displayed.
         * Otherwise the exception property will be printed. If the responseText is not JSON, a generic error message
         * will be displayed
         * @param {Backbone.Model} model configModel
         * @param {jqxhr} xhr The jQuery AJAX object
         */
        handleError: function(model, xhr) {
            this.$('button').removeAttr('disabled');
            this.$ok.html('<i class="icon-save"></i> ' + this.strings.retry);

            try {
                var response = JSON.parse(xhr.responseText);

                if (response.validation) {
                    this.$body.html(this.errorTemplate({validation: response.validation, strings: this.strings}));

                    this.trigger('validation', response.validation);
                } else if (response.exception) {
                    var exceptionMessage = this.strings.errorThrown + ' ' + response.exception;
                    this.$body.html(this.errorTemplate({message: exceptionMessage, strings: this.strings}));
                }
            } catch (e) {
                this.$body.html(this.errorTemplate({
                    message: this.strings.unknown,
                    strings: this.strings
                }));
            }
        },

        /**
         * @desc Handler called when the OK button is clicked. Saves the config model, calling handleSuccess if it
         * succeeds and handleError if it fails
         */
        handleOk: function() {
            this.$('button').attr('disabled', 'disabled');
            this.$body.html(this.throbberTemplate({strings: this.strings}));

            this.configModel.save({config: this.config}, {
                error: this.handleError,
                success: this.handleSuccess,
                wait: true
            });
        },

        /**
         * @desc Handler called when the model is saved successfully
         */
        handleSuccess: function() {
            this.$ok.hide();
            this.$cancel.removeAttr('disabled').html('<i class="icon-remove"></i> ' + this.strings.close);
            this.$body.html(this.successTemplate({strings: this.strings}));
            this.successCallback();
            this.trigger('validation', 'SUCCESS');
        }
    });

});
