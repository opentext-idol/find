define([
    'backbone',
    'find/app/model/find-base-collection'
], function(Backbone, FindBaseCollection) {
    "use strict";

    return FindBaseCollection.extend({
        model: Backbone.Model.extend({
            defaults: {
                values: []
            }
        }),
        
        initialize: function (models, options) {
            this.url = options.url;
        },

        parse: function(response) {
            return _.map(response, function(attributes) {
                return _.extend(attributes, {
                    displayName: attributes.name,
                    values: attributes.values
                });
            });
        }
    });
});
