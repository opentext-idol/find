/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/base-app',
    'find/app/util/logout',
    'find/public/pages',
    'text!find/templates/app/app.html'
], function(BaseApp, logout, Pages, template) {
    return BaseApp.extend({

        template: _.template(template),

        defaultRoute: 'find/search',

        events: {
            'click .navigation-logout': function() {
                logout('../logout');
            }
        },

        initialize: function() {
            this.pages = new Pages();

            BaseApp.prototype.initialize.apply(this, arguments);
        }
    });
});
