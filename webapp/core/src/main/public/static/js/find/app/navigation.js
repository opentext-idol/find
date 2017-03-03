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
            'click .side-nav-menu-button': function(event) {
                event.preventDefault();
                const collapsed = this.sideBarModel.get('collapsed');
                this.sideBarModel.set('collapsed', !collapsed);
            },
            'click a[data-pagename]': function() {
                this.sideBarModel.set('collapsed', true);
            }
        },

        template: _.template(template, {variable: 'data'}),

        menuItems: _.constant(''),

        initialize: function(options) {
            this.pageData = options.pageData;
            this.sideBarModel = new Backbone.Model({collapsed: false});
            this.listenTo(options.router, 'route:page', this.selectPage);

            this.listenTo(vent, 'vent:resize', function() {
                if($(window).width() <= 785 && !this.sideBarModel.get('collapsed')) {
                    this.sideBarModel.set('collapsed', true);
                    this.sideBarModel.set('collapsedFromResize', true);
                }
                else if(this.sideBarModel.get('collapsedFromResize')) {
                    this.sideBarModel.set('collapsed', false);
                    this.sideBarModel.set('collapsedFromResize', false);
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
                .sortBy('order')
                .value();

            this.$el.html(this.template({
                i18n: i18n,
                menuItems: this.menuItems,
                pages: pages,
                username: configuration().username
            }));

            this.$('.side-menu').metisMenu({
                activeClass: 'selected'
            });

            this.listenTo(this.sideBarModel, 'change:collapsed', function(model) {
                this.toggleSideBar(model.get('collapsed'));
            });

            this.sideBarModel.set('collapsed', true);
        },

        selectPage: function(pageName) {
            this.$('li').removeClass('active');

            let $li = this.$('li[data-pagename="' + pageName + '"]');
            $li.addClass('active');
            $li.parents('.find-navbar li').addClass('active');
        }
    });
});
