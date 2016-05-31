define([
    'backbone',
    'find/app/model/find-base-collection'
], function (Backbone, FindBaseCollection) {

    return FindBaseCollection.extend({
        url: '../api/public/parametric',

        model: Backbone.Model.extend({
            idAttribute: 'name',
            defaults: {
                values: []
            }
        }),

        parse: function (response) {
            var parametricArray = _.map(response.parametricValues, function (model) {
                return _.extend({
                    numeric: false
                }, model)
            });

            var numericArray = _.map(response.numericParametricValues, function (model) {
                return _.extend({
                    numeric: true
                }, model)
            });

            return parametricArray.concat(numericArray);
        }

    });
});
