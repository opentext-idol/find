/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module settings/js/controls/enable-view
 */
define([
    'backbone',
    'text!settings/templates/controls/enable-view.html'
], function(Backbone, template) {

    template = _.template(template);

    /**
     * @typedef EnableViewStrings
     * @property {string} enable Text for the button saying it will enable the view
     * @property {string} enabled Label text saying the widget is enabled
     * @property {string} disable Text for the button saying it will disable the view
     * @property {string} disabled Label text saying the widget is disabled
     * @property {string} loading Text for the button saying the configuration is loading
     */
    /**
     * @typedef EnableViewOptions
     * @property {string} enableIcon Classname for the icon of the enable button
     * @property {EnableViewStrings} strings Strings for the view
     */
    /**
     * @name module:settings/js/controls/enable-view.EnableView
     * @desc Utility control for widgets which may be disabled
     * @param {EnableViewOptions} options
     * @constructor
     * @extends Backbone.View
     */
    return Backbone.View.extend(/** @lends module:settings/js/controls/enable-view.EnableView.prototype */ {
        /**
         * @desc Classname for view. Override if using Bootstrap 3
         * @default control-group
         */
        className: 'control-group',

        events: {
            'click button[name=enable]': 'toggleEnabled'
        },

        initialize: function(options) {
            _.bindAll(this, 'getConfig', 'updateConfig', 'updateFormatting');
            this.icon = options.enableIcon;
            this.strings = options.strings;
        },

        /**
         * @desc Renders the view
         */
        render: function() {
            this.$el.html(template({strings: this.strings}));
            this.$button = this.$('button[name=enable]');
        },

        /**
         * @desc Returns the current state of the view
         * @returns {boolean} True if the widget is enabled; false otherwise
         */
        getConfig: function() {
            return this.enabled;
        },

        /**
         * @desc Changes the enabled state to its negation and calls updateFormatting
         * @fires change Event fired with the new state of the view
         */
        toggleEnabled: function() {
            if (!_.isUndefined(this.enabled)) {
                this.enabled = !this.enabled;
                this.trigger('change', this.enabled);
                this.updateFormatting();
            }
        },

        /**
         * @desc Set the state of the view. Calls updateFormatting
         * @param {boolean} config True if the view is enabled; false otherwise
         */
        updateConfig: function(config) {
            var old = this.enabled;
            this.enabled = config;

            if (this.enabled !== old){
                this.trigger('change', this.enabled);
            }

            this.updateFormatting();
        },

        /**
         * @desc Updates the formatting of the view.
         * <p>If the view is enabled, sets the CSS class of the button to btn-danger, the icon to icon-remove, the button
         * text to this.strings.disable, and the label to this.strings.enabled.
         * <p>If the view is disabled, sets the CSS class of the button to btn-success, the icon to options.enableIcon, the
         * button text to this.strings.enable and the label to this.strings.disabled
         */
        updateFormatting: function() {
            this.$button.toggleClass('btn-success', !this.enabled)
                .toggleClass('btn-danger', this.enabled)
                .html(this.enabled ? '<i class="icon-remove"></i> ' + this.strings.disable
                    : '<i class="' + this.icon + '"></i> ' + this.strings.enable)
                .siblings('label').text(
                    this.enabled ? this.strings.enabled
                        : this.strings.disabled);
        }
    });

});
