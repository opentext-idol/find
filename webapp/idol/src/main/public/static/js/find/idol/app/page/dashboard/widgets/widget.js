/*
 * Copyright 2014-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'jquery',
    'text!find/idol/templates/app/page/dashboards/widget.html'
], function(Backbone, _, $, template) {
    'use strict';

    return Backbone.View.extend({

        template: _.template(template),

        initialize: function(options) {
            this.name = options.name;
        },

        render: function() {
            this.$el.html(this.template({
                name: this.name
            }));

            this.$content = this.$('.content');
        },

        contentHeight: function() {
            return this.$content.height();
        },

        contentWidth: function() {
            return this.$content.width();
        },

        update: $.noop,

        onResize: $.noop

    });

});