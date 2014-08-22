define([
    'backbone'
], function(Backbone) {

    return Backbone.Collection.extend({

        url: '../api/search/find-related-concepts',

        fetch: function(options) {
            return Backbone.Collection.prototype.fetch.call(this, _.defaults(options, {
                reset: true
            }));
        }

    })

});
