/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module settings/js/widgets/path-widget
 */
define([
    'settings/js/widget',
    'text!settings/templates/widgets/path-widget.html'
], function(Widget, template) {

    /**
     * @typedef PathWidgetStrings
     * @desc Extends WidgetStrings
     * @property {string} label Label for the input
     * @property {string} validatePathBlank Message displayed if the input is empty
     */
    /**
     * @typedef PathWidgetOptions
     * @desc Extends WidgetOptions
     * @property {PathWidgetStrings} strings Strings for the widget
     */
    /**
     * @name module:settings/js/widgets/path-widget.PathWidget
     * @desc Widget which allows the configuration of a single file path
     * @constructor
     * @param {PathWidgetOptions} options Options for the widget
     * @extends module:settings/js/widget.Widget
     */
    return Widget.extend(/**@lends module:settings/js/widgets/path-widget.PathWidget.prototype */{
        /**
         * @desc CSS classes for the widget.
         * @default {@link module:settings/js/widget.Widget#className|Widget#className} + ' form-horizontal'
         */
        className: Widget.prototype.className + ' form-horizontal',

        /**
         * @typedef PathWidgetTemplateParameters
         * @property {PathWidgetStrings} strings Strings for the template
         */
        /**
         * @callback module:settings/js/widgets/path-widget.PathWidget~PathTemplate
         * @param {PathWidgetTemplateParameters} parameters
         */
        /**
         * @desc Base template for the widget. Override if using Bootstrap 3
         * @type module:settings/js/widgets/path-widget.PathWidget~PathTemplate
         */
        template: _.template(template),

        /**
         * @desc Renders the widget
         */
        render: function() {
            Widget.prototype.render.call(this);
            this.$content.append(this.template({strings: this.strings}));
            this.$path = this.$('input[name="path"]');
        },

        /**
         * @typedef PathConfig
         * @property {string} path The path given by the config
         */
        /**
         * @desc Gets the config represented by the widget
         * @returns {PathConfig} The config represented by the widget
         */
        getConfig: function() {
            //noinspection JSValidateTypes
            return {path: this.$path.val()};
        },

        /**
         * @desc Updates the widget with the given config
         * @param {PathConfig} config
         */
        updateConfig: function(config) {
            Widget.prototype.updateConfig.apply(this, arguments);
            this.$path.val(config.path);
        },

        /**
         * @desc Validates the widget and applies formatting accordingly
         * @returns {boolean} False if the path is empty; true otherwise
         */
        validateInputs: function() {
            if (this.$path.val() === '') {
                this.updateInputValidation(this.$path, false);
                return false;
            }

            return true;
        }
    });

});
