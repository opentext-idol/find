define([
    'backbone',
    'find/app/model/saved-searches/saved-search-model'
], function(Backbone, SavedSearchModel) {

    return Backbone.Collection.extend({
        url: '../api/public/saved-snapshot',
        model: SavedSearchModel.extend({
            defaults: {
                type: 'snapshot'
            }
        })
    });

});
