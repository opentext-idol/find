/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'backbone',
    'find/app/util/test-browser',
    'find/app/navigation',
    'find/app/configuration',
    'find/app/util/logout',
    'find/app/vent',
    'text!find/templates/app/app.html'
], function($, Backbone, testBrowser, Navigation, configuration, logout, vent, template) {

    return Backbone.View.extend({
        el: '.page',
        template: _.template(template),
        defaultRoute: 'find/search/splash',
        Navigation: Navigation,

        events: {
            'click .navigation-logout': function() {
                logout('../logout');
            }
        },

        initialize: function() {
            $.ajaxSetup({cache: false});

            this.pages = this.constructPages();

            this.navigation = new this.Navigation({
                pages: this.pages
            });

            this.render();

            var matchedRoute = Backbone.history.start();

            if (!matchedRoute) {
                vent.navigate(this.defaultRoute);
            }

            testBrowser();
        },

        // will be overridden
        constructPages: $.noop(),

        render: function() {
            this.$el.html(this.template({
                username: configuration().username
            }));

            this.pages.render();

            this.$('.content').append(this.pages.el);

            this.navigation.render();

            this.$('.header').prepend(this.navigation.el);
        }
    });

});
