/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'text!find/templates/app/page/search/abstract-section-view.html'
], function(Backbone, template) {
    'use strict';

    return Backbone.View.extend({

        baseTemplate: _.template(template),

        initialize: function(options) {
            this.title = options.title;
            this.titleClass = options.titleClass;
        },

        render: function() {
            this.$el.html(this.baseTemplate({
                title: this.title,
                titleClass: this.titleClass
            }))
        },

        getViewContainer: function() {
            return this.$('.left-view-container');
        },

        getSectionControls: function() {
            return this.$('.section-controls');
        }

    });

});