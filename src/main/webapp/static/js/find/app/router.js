define([
    'backbone'
], function(Backbone) {
    var Router = Backbone.Router.extend({

        routes: {
            'find/search(/:text)': 'search',
            'find/:page': 'find'
        },

        navigate: function() {
            $('.modal').not('.undismissable-modal').modal('hide');

            return Backbone.Router.prototype.navigate.apply(this, arguments);
        },

        search: function() {
            this.trigger('route:find', 'search');
        }

    });

    return new Router();

});