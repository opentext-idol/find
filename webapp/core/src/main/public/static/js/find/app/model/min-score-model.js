define([
    'backbone'
], function(Backbone) {

    return Backbone.Model.extend({
        defaults: {
            minScore: 0
        }
    });
});