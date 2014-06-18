define([
    'backbone'
], function(Backbone) {
    var Router = Backbone.Router.extend({

        routes: {
            'find/:page': 'find'
        },

        navigate: function() {
            $('.modal').not('.undismissable-modal').modal('hide');

            return Backbone.Router.prototype.navigate.apply(this, arguments);
        }

    });

    return new Router();
})