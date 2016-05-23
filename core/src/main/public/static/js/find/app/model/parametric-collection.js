define([
    'backbone',
    'find/app/model/find-base-collection',
    'find/app/util/search-data-util'
], function (Backbone, FindBaseCollection, searchDataUtil) {

    return Backbone.Collection.extend({
        sync: FindBaseCollection.prototype.sync,

        url: '../api/public/parametric',

        model: Backbone.Model.extend({
            idAttribute: 'name',
            defaults: {
                values: []
            }
        }),

        initialize: function (models, options) {
            this.indexesCollection = options.indexesCollection;
        },

        fetch: function () {
            Backbone.Collection.prototype.fetch.call(this, {
                reset: true,
                data: {
                    databases: searchDataUtil.buildIndexes(this.indexesCollection.map(function (model) {
                        return model.pick('domain', 'name');
                    }))
                }
            });
        },

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
