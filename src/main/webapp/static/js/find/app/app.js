/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/base-app',
    'find/app/pages',
    'find/app/navigation',
    'text!find/templates/app/private-app.html'
], function(BaseApp, Pages, Navigation, template) {

    return BaseApp.extend({

        template: _.template(template),

        defaultRoute: 'find/settings',

        initialize: function() {
            this.pages = new Pages();

            this.navigation = new Navigation({
                pages: this.pages
            });

            BaseApp.prototype.initialize.apply(this, arguments);
        },

        render: function() {
            BaseApp.prototype.render.apply(this, arguments);

            this.navigation.render();

            this.$('.header').append(this.navigation.el);
        }

    });

});