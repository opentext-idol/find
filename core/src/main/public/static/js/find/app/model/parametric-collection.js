define([
    'backbone',
    'find/app/model/find-base-collection'
], function(Backbone, FindBaseCollection) {
    "use strict";

    return FindBaseCollection.extend({
        model: Backbone.Model.extend({
            idAttribute: 'name',
            defaults: {
                values: []
            }
        }),
        
        initialize: function (models, options) {
            this.url = options.url;
        }
    });
});
