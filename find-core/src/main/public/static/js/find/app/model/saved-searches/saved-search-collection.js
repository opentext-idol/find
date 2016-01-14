define([
    'find/app/model/saved-search-collection',
    'backbone',
    'underscore'
], function(SavedSearchModel, Backbone, _) {

    return Backbone.Collection.extend({
        model: SavedSearchModel


    })
});
