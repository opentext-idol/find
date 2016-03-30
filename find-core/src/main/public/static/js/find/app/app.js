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
    'find/app/pages',
    'find/app/util/logout',
    'find/app/vent',
    'find/app/router',
    'text!find/templates/app/app.html'
], function($, Backbone, testBrowser, Navigation, configuration, Pages, logout, vent, router, template) {

    return Backbone.View.extend({
        el: '.page',
        template: _.template(template),
        Navigation: Navigation,

        // Abstract
        getPageData: null,

        events: {
            'click .navigation-logout': function() {
                logout('../logout');
            }
        },

        initialize: function() {
            $.ajaxSetup({cache: false});

            var pageData = this.getPageData();
            this.pages = new Pages({pageData: pageData, router: router});
            this.navigation = new this.Navigation({pageData: pageData, router: router});

            this.render();

            var matchedRoute = Backbone.history.start();

            if (!matchedRoute) {
                vent.navigate('find/search/splash');
            }

            testBrowser();
        },

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
