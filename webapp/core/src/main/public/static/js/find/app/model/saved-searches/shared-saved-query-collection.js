const Backbone = require('backbone');
const FindBaseCollection = require('find/app/model/find-base-collection');
const SavedSearchModel = require('find/app/model/saved-searches/saved-search-model');
const _ = require('underscore');

module.exports = FindBaseCollection.extend({
        url: 'api/bi/saved-query/shared',

        parse: function(response) {
            return response.savedQueries;
        },

        model: SavedSearchModel.extend({
            parse: function(response) {
                const parsedResponse = SavedSearchModel.prototype.parse.call(this, response);

                 return _.extend({
                     type: parsedResponse.canEdit ? SavedSearchModel.Type.SHARED_QUERY : SavedSearchModel.Type.SHARED_READ_ONLY_QUERY
                 }, parsedResponse)
            }
        })
    });

