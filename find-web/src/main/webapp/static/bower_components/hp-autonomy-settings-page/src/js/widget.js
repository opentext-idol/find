/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module settings/js/widget
 */
define([
    '../../../backbone/backbone',
    'text!settings/templates/widget.html'
], function(Backbone, template) {

    /**
     * @typedef WidgetStrings
     * @property {string} iconClass Icon to use in the title bar for the widget
     */
    /**
     * @typedef WidgetOptions
     * @property {string} configItem The section of the configuration represented by this widget
     * @property {string} description A description for the widget
     * @property {string} serverName (deprecated) Name of the server
     * @property {WidgetStrings} strings Internationalisation strings for the widget
     * @property {string} title Title for the widget
     * @property {boolean} isOpened True if the widget should start opened; false otherwise
     */
    /**
     * @name module:settings/js/widget.Widget
     * @desc Base class for widgets. Extend this to add your own functionality
     * @constructor
     * @param {WidgetOptions} options Options for the widget
     * @abstract
     * @extends Backbone.View
     */
    return Backbone.View.extend(/**@lends module:settings/js/widget.Widget.prototype */ {
        /**
         * @desc Classes applied to the widget. Override if using Bootstrap 3
         * @type string
         * @default row-fluid accordion-group
         */
        className: 'row-fluid accordion-group',

        /**
         * @desc Returns the config associated with the widget
         * @abstract
         * @method
         */
        getConfig: $.noop,

        /**
         * @desc Class applied to control groups. Set to 'form-group' if using Bootstrap 3
         * @default control-group
         */
        controlGroupClass: 'control-group',

        /**
         * @desc Class applied to form controls. Set to 'form-control' if using Bootstrap 3
         * @default ''
         */
        formControlClass: '',

        /**
         * @desc Class used to indicate successful validation. Set to 'has-success' if using Bootstrap 3
         * @default success
         */
        successClass: 'success',

        /**
         * @desc Class used to indicate failed validation. Set to 'has-error' if using Bootstrap 3
         * @default error
         */
        errorClass: 'error',

        /**
         * @typedef WidgetTemplateParameters
         * @property {string} description this.description
         * @property {string} title this.title
         * @property {string} configItem this.configItem
         * @property {boolean} isOpened this.isOpened
         * @property {string} iconClass this.strings.iconClass
         */
        /**
         * @callback module:settings/js/widget.Widget~WidgetTemplate
         * @param {WidgetTemplateParameters} parameters
         */
        /**
         * @desc Base template for the widget. Override if using Bootstrap 3
         * @type module:settings/js/widget.Widget~WidgetTemplate
         */
        widgetTemplate: _.template(template),

        events: {
            'change input,select': 'handleInputChange'
        },

        initialize: function(options) {
            _.bindAll(this, 'getConfig', 'getName', 'handleValidation', 'shouldValidate', 'updateConfig', 'validateInputs');

            if (!options.configItem) {
                throw 'Settings Widget Exception: A config item must be provided.';
            }

            this.configItem = options.configItem;
            this.description = options.description;
            this.serverName = options.serverName;
            this.strings = options.strings;
            this.title = options.title;
            this.isOpened = options.isOpened;
        },

        /**
         * @desc Renders the widget
         */
        render: function() {
            this.$el.html(this.widgetTemplate({
                description: this.description,
                title: this.title,
                configItem: this.configItem,
                isOpened: this.isOpened,
                iconClass: this.strings.iconClass
            }));

            this.$content = this.$('.widget-content');
        },

        /**
         * @desc Gets the name for the widget
         * @returns {string} this.serverName, or this.configItem if serverName is undefined
         * @deprecated Users of this class do not need to call this
         */
        getName: function() {
            return this.serverName || this.configItem;
        },

        /**
         * @desc Handler for change events on any inputs in the widget. Calls hideValidationInfo and
         * setValidationFormatting
         */
        handleInputChange: function() {
            this.hideValidationInfo();
            this.setValidationFormatting('clear');
        },

        /**
         * @typedef ValidationResponse
         * @property {boolean} valid True if the configuration is valid; false otherwise
         * @property {*} [data] An object with additional detail about the validation
         */
        /**
         * @desc Handles the results of server side validation (several widgets have a Test Connection button to do
         * this). Override this method if your widget needs server side validation
         * @param {object} config The configuration object for the widget
         * @param {ValidationResponse} response The validation response from the server
         * @returns {boolean} True if the widget is valid; false otherwise
         * @abstract
         * @method
         */
        handleValidation: $.noop,

        /**
         * @desc Hides any validation information (elements with the settings-client-validation class)
         */
        hideValidationInfo: function() {
            this.$('.settings-client-validation').addClass('hide');
        },

        /**
         * @desc Removes this.successClass and this.errorClass from elements in the widget with this.controlGroupClass.
         * <p> If state === 'clear', remove successClass and errorClass from this.$el. Otherwise adds the given class
         * and removes the other class
         * @param {string} state clear to remove classes, otherwise this.successClass or this.errorClass to add that
         * class and remove the other class
         */
        setValidationFormatting: function(state) {
            this.$el.find('.' + this.controlGroupClass).removeClass(this.successClass + ' ' + this.errorClass);

            if (state === 'clear') {
                this.$el.removeClass(this.successClass + ' ' + this.errorClass);
            } else {
                this.$el.addClass(state)
                    .removeClass(state === this.successClass ? this.errorClass : this.successClass);
            }
        },

        /**
         * @desc Returns false by default; override it to return true if your widget needs client side validation
         * @returns {boolean} True if the widget should validate; false otherwise
         * @abstract
         */
        shouldValidate: function() {
            return false;
        },

        /**
         * @desc Updates the state of the widget. Subclasses should override this method, call this method and then
         * perform additional operations
         * @abstract
         */
        updateConfig: function() {
            this.setValidationFormatting('clear');
            this.hideValidationInfo();
        },

        /**
         * @desc Updates the formatting of the given input
         * <p> If isValid is true, removes errorClass from the input and hides elements with the class
         * settings-client-validation
         * <p> If isValid is false, adds errorClass to the input and shows elements with the class
         * settings-client-validation
         * @param {jQuery} $input The input to apply the classes to
         * @param {boolean} isValid True if the input is valid; false otherwise
         * @protected
         */
        updateInputValidation: function($input, isValid) {
            var $controlGroup = $input.closest('.' + this.controlGroupClass);
            var $span = $controlGroup.find('.settings-client-validation');

            if (isValid) {
                $controlGroup.removeClass(this.errorClass);
                $span.addClass('hide');
            } else {
                $controlGroup.addClass(this.errorClass);
                $span.removeClass('hide');
            }
        },

        /**
         * @desc Validates the inputs in the widget. Default implementation just returns true - override if you need
         * validation. This method should perform any formatting required to display the validation failure.
         * @returns {boolean} True if the widget is valid; false otherwise
         * @abstract
         */
        validateInputs: function() {
            return true;
        }
    });

});
