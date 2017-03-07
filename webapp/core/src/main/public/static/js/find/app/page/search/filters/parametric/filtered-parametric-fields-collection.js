define([
    'backbone',
    'underscore',
    'find/app/util/filtering-collection',
], function(Backbone, _, FilteringCollection) {
    'use strict';

    function filterPredicate(model, filterModel) {
        const searchText = filterModel && filterModel.get('text');
        return !searchText || searchMatches(model.get('displayName'), filterModel.get('text'));
    }

    function searchMatches(text, search) {
        return text.toLowerCase().indexOf(search.toLowerCase()) > -1;
    }

    return FilteringCollection.extend({
        initialize: function (models, options) {
            FilteringCollection.prototype.initialize.apply(this, [models, _.defaults({ predicate: filterPredicate }, options)]);
        }
    });
});