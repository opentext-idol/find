define([
    'backbone',
    'find/app/model/saved-searches/saved-search-model'
], function(Backbone, SavedSearchModel) {

    return Backbone.Collection.extend({
        url: '../api/public/saved-query',

        model: SavedSearchModel.extend({
            defaults: _.defaults({
                type: SavedSearchModel.Type.QUERY
            }, SavedSearchModel.prototype.defaults)
        })
    });

});
