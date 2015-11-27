define([
    'backbone',
    'find/app/model/find-base-collection'
], function(Backbone, FindBaseCollection) {

    return FindBaseCollection.extend({
        url: '../api/public/parametric',

        model: Backbone.Model.extend({
            idAttribute: 'name',

            defaults: {
                values: []
            }
        })
    });
});
