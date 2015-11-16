/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module settings/js/widgets/tasks-widget
 */
define([
    'settings/js/widget',
    'text!settings/templates/widgets/tasks-widget.html'
], function(Widget, template) {

    template = _.template(template);

    /**
     * @typedef TasksWidgetStrings
     * @desc Extends WidgetStrings
     * @property {string} dashboardPrefix Label for the dashboard history option
     * @property {string} day Singular for day
     * @property {string} days Plural for days
     * @property {string} footerPrefix Label for the footer history option
     * @property {string} keepTasks Label for the options to store completed tasks for a given period of time
     * @property {string} keepTasksForever Label for the options to store completed tasks forever
     * @property {string} min Singular for minute
     * @property {string} mins Plural for minute
     */
    /**
     * @typedef TasksWidgetOptions
     * @desc Extends WidgetOptions
     * @property {TasksWidgetStrings} strings Strings for the widget
     */
    /**
     * @name module:settings/js/widgets/tasks-widget.TasksWidget
     * @desc Widget for configuring how long task history should be kept for. Users configure times in minutes
     * @constructor
     * @param {TasksWidgetOptions} options Options for the widget
     * @extends module:settings/js/widget.Widget
     */
    return Widget.extend(/** @lends module:settings/js/widgets/tasks-widget.TasksWidget.prototype */ {
        /**
         * @desc CSS classes for the widget.
         * @default {@link module:settings/js/widget.Widget#className|Widget#className} + ' form-horizontal'
         */
        className: Widget.prototype.className + ' form-horizontal',

        events: {
            'change input[name=dashboard-history-mins]': 'processPlurals',
            'change input[name=footer-history-mins]': 'processPlurals',
            'change input[name=history-days]': 'processPlurals'
        },

        initialize: function(options) {
            Widget.prototype.initialize.call(this, options);
            _.bindAll(this, 'processPlurals');
        },

        /**
         * @desc Renders the widget
         */
        render: function() {
            Widget.prototype.render.call(this);
            this.$content.append(template({strings: this.strings}));

            this.$dashboard = this.$('input[name=dashboard-history-mins]');
            this.$footer = this.$('input[name=footer-history-mins]');
            this.$history = this.$('input[name=history-days]');

            this.$dashboardSpan = this.$dashboard.siblings('span');
            this.$footerSpan = this.$footer.siblings('span');
            this.$historySpan = this.$history.siblings('span');
        },

        /**
         * @typedef TasksWidgetConfig
         * @property {number} dashboardHistorySecs The number of seconds tasks should be kept on the dashboard
         * @property {number} footerHistorySecs The number of seconds tasks should be kept in the footer
         * @property {number} historySecs The number of seconds tasks should be kept in the database. A negative number
         * means the task will be kept indefinitely
         */
        /**
         * @desc Gets the configuration associated with the widget
         * @returns {TasksWidgetConfig} The configuration associated with the widget
         */
        getConfig: function() {
            var keepHistoryForever = this.$('input[name=history-forever][value=true]').prop('checked');
            var historySecs = Math.abs(this.$history.val()) * 86400;

            //noinspection JSValidateTypes
            return {
                dashboardHistorySecs: this.$dashboard.val() * 60,
                footerHistorySecs: this.$footer.val() * 60,
                historySecs: keepHistoryForever ? -historySecs : historySecs
            };
        },

        /**
         * @desc Updates quantifiers to be plurals if the corresponding input is not equal to 1
         * @deprecated This will be removed in future as it internationalises badly
         * @protected
         */
        processPlurals: function() {
            this.$dashboardSpan.html(Number(this.$dashboard.val()) === 1 ? this.strings.min : this.strings.mins);
            this.$footerSpan.html(Number(this.$footer.val()) === 1 ? this.strings.min : this.strings.mins);
            this.$historySpan.html(Number(this.$history.val()) === 1 ? this.strings.day : this.strings.days);
        },

        /**
         * @desc Updates the widget with the given configuration
         * @param {TasksWidgetConfig} config The new configuration for the widget
         */
        updateConfig: function(config) {
            var dashboardMins = Number((config.dashboardHistorySecs / 60).toFixed(0));
            var footerMins = Number((config.footerHistorySecs / 60).toFixed(0));
            var historyDays = Number((Math.abs(config.historySecs) / 86400).toFixed(1));

            this.$dashboard.val(dashboardMins);
            this.$footer.val(footerMins);
            this.$history.val(historyDays);
            this.$('input[name=history-forever][value=' + (config.historySecs < 0) + ']').prop('checked', true);

            this.processPlurals();
        }
    });

});
