/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'underscore',
    'backbone',
    'i18n!find/nls/bundle',
    'text!find/idol/templates/page/dashboards/widget.html'
], function(_, Backbone, i18n, template) {
    'use strict';

    return Backbone.View.extend({
        clickable: false,
        template: _.template(template),

        isUpdating: _.constant(false),
        onResize: _.noop,
        onHide: _.noop,
        onClick: _.noop,

        initialize: function(options) {
            this.name = options.name;
            this.widgetSettings = options.widgetSettings || {};
            this.cssClass = options.cssClass;
        },

        render: function() {
            this.$el.html(this.template({
                name: this.name,
                i18n: i18n
            }));

            if (this.cssClass) {
                this.$el.addClass(this.cssClass);
            }

            if(this.clickable) {
                this.$el.click(this.onClick.bind(this));
            }

            this.$content = this.$('.widget-content');
            this.$error = this.$('.widget-error');
            this.$empty = this.$('.widget-empty');
        },

        contentHeight: function() {
            return this.$content.height();
        },

        contentWidth: function() {
            return this.$content.width();
        }
    });
});
