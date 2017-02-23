/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone',
    'text!find/idol/templates/page/dashboards/widget.html'
], function(_, Backbone, template) {
    'use strict';

    return Backbone.View.extend({
        viewType: '',
        clickable: false,
        template: _.template(template),

        isUpdating: _.constant(false),
        onResize: _.noop,
        onClick: _.noop,

        initialize: function(options) {
            this.name = options.name;
        },

        render: function() {
            this.$el.html(this.template({
                name: this.name
            }));

            if(this.clickable) {
                this.$el.click(this.onClick.bind(this));
            }

            this.$content = this.$('.widget-content').addClass(this.viewType);
        },

        contentHeight: function() {
            return this.$content.height();
        },

        contentWidth: function() {
            return this.$content.width();
        }
    });
});
