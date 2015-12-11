/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/base-app',
    'find/app/util/logout',
    'find/app/configuration',
    'find/public/pages',
    'text!find/templates/app/app.html'
], function(BaseApp, logout, configuration, Pages, template) {
    "use strict";

    return BaseApp.extend({
        // will be overridden
        constructPages: function () {
            return new Pages();
        },

        template: _.template(template),

        defaultRoute: 'find/search',

        events: {
            'click .navigation-logout': function() {
                logout('../logout');
            }
        },

        initialize: function() {
            this.pages = this.constructPages();

            BaseApp.prototype.initialize.apply(this, arguments);
        },

        getTemplateParameters: function() {
            return {
                username: configuration().username
            };
        }
    });
});
