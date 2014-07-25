define([
    'backbone',
    'find/app/pages',
    'find/app/util/test-browser',
    'find/app/vent',
    'text!find/templates/app/app.html'
], function(Backbone, Pages, testBrowser, vent, template) {
    return Backbone.View.extend({

        el: '.page',

        template: _.template(template),

        initialize: function() {
            jQuery.ajaxSetup({ cache: false });

            this.pages = new Pages();

            this.render();

            Backbone.history.start();

            if (!window.location.hash || window.location.hash === "#undefined" || window.location.hash === "undefined") {
                vent.navigate('find/find-search');
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