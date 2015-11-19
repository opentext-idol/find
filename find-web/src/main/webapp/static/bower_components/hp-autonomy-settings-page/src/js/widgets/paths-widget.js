/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module settings/js/widgets/paths-widget
 */
define([
    'settings/js/widget',
    'backbone',
    'js-whatever/js/list-view',
    'text!settings/templates/widgets/paths-widget.html',
    'text!settings/templates/widgets/paths-widget-row.html'
], function(Widget, Backbone, ListView, template, itemTemplate) {

    /**
     * @typedef PathsWidgetStrings
     * @desc Extends WidgetStrings
     * @property {string} addPath Label for the add path button
     * @property {string} validatePathBlank Message displayed when a path is empty
     */
    /**
     * @typedef PathsWidgetOptions
     * @desc Extends WidgetOptions
     * @property {PathsWidgetStrings} strings Strings for the widget
     */
    /**
     * @name module:settings/js/widgets/paths-widget.PathsWidget
     * @desc Widget which allows the configuration of multiple file paths
     * @constructor
     * @param {PathsWidgetOptions} options Options for the widget
     * @extends module:settings/js/widget.Widget
     */
    return Widget.extend(/** @lends module:settings/js/widgets/paths-widget.PathsWidget.prototype */{
        /**
         * @typedef PathsWidgetTemplateParameters
         * @property {PathsWidgetStrings} strings Strings for the template
         */
        /**
         * @callback module:settings/js/widgets/paths-widget.PathsWidget~PathsTemplate
         * @param {PathsWidgetTemplateParameters} parameters
         */
        /**
         * @desc Template for each individual row. Override if using Bootstrap 3
         * @type module:settings/js/widgets/path-widget.PathWidget~PathTemplate
         */
        itemTemplate: _.template(itemTemplate),

        /**
         * @desc Base template for the widget. Override if using Bootstrap 3
         * @type module:settings/js/widgets/paths-widget.PathsWidget~PathsTemplate
         */
        template: _.template(template),

        events: _.extend({
            'change [name="path"]': function(e) {
                var $input = $(e.currentTarget);
                var cid = $input.closest('[data-cid]').data('cid');
                this.collection.get(cid).set('path', $input.val());
            },
            'click [name="remove-path"]': function(e) {
                var cid = $(e.currentTarget).closest('[data-cid]').data('cid');
                this.collection.remove(this.collection.get(cid));
            },
            'click [name="add-path"]': function() {
                this.collection.add({path: ''});
            }
        }, Widget.prototype.events),

        initialize: function() {
            Widget.prototype.initialize.apply(this, arguments);
            this.collection = new Backbone.Collection();

            this.listView = new ListView({
                collection: this.collection,
                itemTemplate: this.itemTemplate,
                itemTemplateOptions: {strings: this.strings}
            });

            this.listenTo(this.collection, 'add remove reset', this.updateRemoveButtons);
        },

        /**
         * @desc Renders the widget
         */
        render: function() {
            Widget.prototype.render.call(this);
            this.$content.append(this.template({strings: this.strings}));
            this.listView.setElement(this.$('.path-list')).render();
            this.updateRemoveButtons();
        },

        /**
         * @typedef PathsConfig
         * @property {string[]} paths The array of configured paths
         */
        /**
         * @desc Gets the configuration associated with the widget
         * @returns {PathsConfig} The configuration associated with the widget
         */
        getConfig: function() {
            //noinspection JSValidateTypes
            return {paths: this.collection.pluck('path')};
        },

        /**
         * @desc Updates the widget with the given configuration
         * @param {PathsConfig} config The new configuration
         */
        updateConfig: function(config) {
            Widget.prototype.updateConfig.apply(this, arguments);

            this.collection.reset(_.map(config.paths, function(path) {
                return {path: path};
            }));
        },

        /**
         * @desc Disables the remove buttons when the number of inputs reaches 1, enables them otherwise
         * @protected
         */
        updateRemoveButtons: function() {
            this.$('[name="remove-path"]').prop('disabled', this.collection.length === 1);
        },

        /**
         * @desc Validates the widget and applies formatting accordingly
         * @returns {boolean} False if any of the paths is empty; true otherwise
         */
        validateInputs: function() {
            var isValid = true;

            this.collection.each(function(model) {
                if (!model.get('path')) {
                    isValid = false;
                    var $item = this.listView.$('[data-cid="' + model.cid + '"]');
                    $item.addClass('error');
                    this.updateInputValidation($item.find('input'), false);
                }
            }, this);

            return isValid;
        }
    });

});