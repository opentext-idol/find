define([
    'backbone',
    'find/app/model/find-base-collection'
], function(Backbone, FindBaseCollection) {

    var prettifyFieldName = function(name) {
        return _.map(name.split("_"), function(word) {
            return word[0].toUpperCase() + word.slice(1);
        }).join(" ");
    };

    return FindBaseCollection.extend({
        url: '../api/public/parametric',

        comparator: 'name',

        model: Backbone.Model.extend({
            idAttribute: 'name',

            parse: function(response, options) {
                return {
                    name: response.name,
                    displayName: prettifyFieldName(response.name),
                    values: _.sortBy(response.values, function (value) {
                        return -1 * value.count;
                    })
                }
            },

            defaults: {
                values: []
            }
        })
    });
});
