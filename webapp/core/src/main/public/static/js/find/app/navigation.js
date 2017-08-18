/*
 * Copyright 2014-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'find/app/vent',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'text!find/templates/app/navigation.html',
    'metisMenu'
], function(_, $, Backbone, vent, configuration, i18n, template) {
    'use strict';

    return Backbone.View.extend({
        events: {
            'click .find-logo-small.clickable': function(event) {
                event.preventDefault();
                window.open('public/search/splash', '_self');
            },
            'click .nav-menu-toggle-btn': function(event) {
                event.preventDefault();
                this.sidebarModel.set('collapsed', !this.sidebarModel.get('collapsed'));
            },
            'click a[data-pagename]': function() {
                this.sidebarModel.set('collapsed', true);
            }
        },

        template: _.template(template, {variable: 'data'}),

        menuItems: _.constant(''),

        initialize: function(options) {
            this.pageData = options.pageData;
            this.sidebarModel = options.sidebarModel;
            this.listenTo(options.router, 'route:page', this.selectPage);

            this.listenTo(vent, 'vent:resize', function() {
                if($(window).width() <= 785 && !this.sidebarModel.get('collapsed')) {
                    this.sidebarModel.set('collapsed', true);
                    this.sidebarModel.set('collapsedFromResize', true);
                } else if(this.sidebarModel.get('collapsedFromResize')) {
                    this.sidebarModel.set('collapsed', false);
                    this.sidebarModel.set('collapsedFromResize', false);
                }
            });
        },

        toggleSideBar: function(collapsed) { // side is for when not collapsed
            $(document.body).toggleClass('hide-navbar', collapsed);
        },

        render: function() {
            const pages = _.chain(this.pageData)
                .map(function(data, name) {
                    return _.extend({pageName: name}, data);
                })
                .filter(function(page) {
                    return _.has(page, 'navigation');
                })
                .sortBy('order')
                .groupBy('navigation')
                .value();

            this.$el.html(this.template({
                i18n: i18n,
                menuItems: this.menuItems,
                pages: pages,
                applications: configuration().applications,
                username: configuration().username,
                // If we're a normal user, clicking the logo should send us to the splash page.
                //   (which basically reloads the page and clears all the filters, since there's no route for it).
                splashLink: !configuration().hasBiRole
            }));

            this.$('.side-menu').metisMenu({
                activeClass: 'selected'
            });

            this.listenTo(this.sidebarModel, 'change:collapsed', function(model) {
                this.toggleSideBar(model.get('collapsed'));
            });

            this.sidebarModel.set('collapsed', true);
        },

        selectPage: function(pageName) {
            this.$('li .active').removeClass('active');

            this.$('li[data-pagename="' + pageName + '"]')
                .addClass('active')
                .parents('.find-navbar li')
                .addClass('active');
        }
    });
});
