/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
