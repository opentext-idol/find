/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone',
    'jquery',
    'find/app/vent',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'text!find/templates/app/navigation.html'
], function(_, Backbone, $, vent, configuration, i18n, template) {
    "use strict";
    return Backbone.View.extend({
        events: {
            'click .top-nav-menu-button': function (event) {
                event.preventDefault();
                this.sideBarModel.set('collapsed', false);
            }
        },

        template: _.template(template, {variable: 'data'}),

        menuItems: _.constant(''),

        initialize: function(options) {
            this.pageData = options.pageData;
            this.sideBarModel = options.sideBarModel;
            this.listenTo(options.router, 'route:page', this.selectPage);
        },

        render: function() {
            var pages = _.chain(this.pageData)
                .map(function(data, name) {
                    return _.extend({pageName: name}, data);
                })
                .sortBy('order')
                .value();

            this.$el.html(this.template({
                i18n: i18n,
                menuItems: this.menuItems,
                pages: pages,
                username: configuration().username
            }));
        },

        selectPage: function(pageName) {
            this.$('li').removeClass('active');
            this.$('li[data-pagename="' + pageName + '"]').addClass('active');
        }
    });

});
