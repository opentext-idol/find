define([
    'backbone',
    'find/app/util/test-browser',
    'find/app/vent'
], function(Backbone, testBrowser, vent) {
    return Backbone.View.extend({

        el: '.page',

        initialize: function() {
            jQuery.ajaxSetup({ cache: false });

            this.render();

            Backbone.history.start();

            if (!window.location.hash || window.location.hash === "#undefined" || window.location.hash === "undefined") {
                vent.navigate(this.defaultRoute);
            }

            testBrowser();
        },

        render: function() {
            this.$el.html(this.template());

            this.pages.render();

            this.$('.content').append(this.pages.el);
        }

    });
});