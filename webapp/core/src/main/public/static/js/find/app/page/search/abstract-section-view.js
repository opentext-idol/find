/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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
