define([
    'find/app/base-app',
    'find/public/pages',
    'text!find/templates/app/app.html'
], function(BaseApp, Pages, template) {

    return BaseApp.extend({

        template: _.template(template),

        defaultRoute: 'find/search',

        initialize: function() {
            this.pages = new Pages();

            BaseApp.prototype.initialize.apply(this, arguments);
        }

    });

});