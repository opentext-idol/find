define([
    'backbone',
    'find/app/pages',
    'find/app/navigation',
    'find/app/footer',
    'find/app/util/test-browser',
    'find/app/vent',
    'text!find/templates/app/app.html'
], function(Backbone, Pages, Navigation, Footer, testBrowser, vent, template) {
    return Backbone.View.extend({

        el: '.page',

        template: _.template(template),

        initialize: function() {
            jQuery.ajaxSetup({ cache: false });

            this.pages = new Pages();

            this.navigation = new Navigation({
                pages: this.pages
            });

            this.footer = new Footer({
                $parent: this.$el
            });

            this.render();

            Backbone.history.start();

            if (!window.location.hash || window.location.hash === "#undefined" || window.location.hash === "undefined") {
                vent.navigate('find/first-page');
            }

            testBrowser();
        },

        render: function() {
            this.$el.html(this.template());

            this.navigation.render();
            this.pages.render();
            this.footer.render();

            this.$('.header').append(this.navigation.el);
            this.$('.content').append(this.pages.el);
            this.$('.footer').append(this.footer.el);
        }

    });
});