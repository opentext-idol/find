/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/base-app',
    'find/public/navigation',
    'find/app/configuration',
    'find/app/util/logout',
    'text!find/templates/app/app.html'
], function(BaseApp, Navigation, configuration, logout, template) {

    return BaseApp.extend({

        template: _.template(template),

        defaultRoute: 'find/search/splash',

        Navigation: Navigation,

        events: {
            'click .navigation-logout': function() {
                logout('../logout');
            }
        },

        initialize: function() {
            this.pages = this.constructPages();

            this.navigation = new this.Navigation({
                pages: this.pages
            });

            BaseApp.prototype.initialize.apply(this, arguments);
        },

        // will be overridden
        constructPages: $.noop(),

        render: function() {
            BaseApp.prototype.render.apply(this, arguments);

            this.navigation.render();

            this.$('.header').prepend(this.navigation.el);
        },

        getTemplateParameters: function() {
            return {
                username: configuration().username
            };
        }
    });
});
