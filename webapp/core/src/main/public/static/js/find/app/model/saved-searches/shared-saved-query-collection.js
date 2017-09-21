define([
    'backbone',
    'find/app/model/find-base-collection',
    'find/app/model/saved-searches/saved-search-model',
    'underscore'
], function(Backbone, FindBaseCollection, SavedSearchModel, _) {
    'use strict';

    return FindBaseCollection.extend({
        url: 'api/bi/saved-query/shared',

        model: SavedSearchModel.extend({
            parse: function(response) {
                const parsedResponse = SavedSearchModel.prototype.parse.call(this, response);

                 return _.extend({
                     type: parsedResponse.canEdit ? SavedSearchModel.Type.SHARED_QUERY : SavedSearchModel.Type.SHARED_READ_ONLY_QUERY
                 }, parsedResponse)
            }
        })
    });
});
