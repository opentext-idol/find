/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'find/app/util/test-browser',
    'find/app/vent'
], function(Backbone, $, testBrowser, vent) {

    return Backbone.View.extend({
        el: '.page',

        initialize: function() {
            $.ajaxSetup({ cache: false });

            this.render();

            var matchedRoute = Backbone.history.start();

            if (!matchedRoute) {
                vent.navigate(this.defaultRoute);
            }

            testBrowser();
        },

        render: function() {
            this.$el.html(this.template(this.getTemplateParameters()));

            this.pages.render();

            this.$('.content').append(this.pages.el);
        },

        getTemplateParameters: function() {
            return {};
        }
    });

});
