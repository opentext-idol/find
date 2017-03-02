/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone',
    'text!find/templates/app/page/search/abstract-section-view.html'
], function(_, Backbone, template) {
    'use strict';

    return Backbone.View.extend({
        baseTemplate: _.template(template),

        initialize: function(options) {
            this.title = options.title;
            this.titleClass = options.titleClass;
            this.containerClass = options.containerClass;// For testing purposes
        },

        render: function() {
            this.$el.html(this.baseTemplate({
                section: {
                    title: this.title,
                    titleClass: this.titleClass,
                    containerClass: this.containerClass
                }
            }))
        },

        getViewContainer: function() {
            return this.$('.left-view-container');
        },

        getHeaderCounter: function() {
            return this.$('.section-title-counter');
        },

        getSectionControls: function() {
            return this.$('.section-controls');
        }
    });
});
