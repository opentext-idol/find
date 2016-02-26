/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/saved-searches/saved-search-model',
    'i18n!find/nls/bundle',
    'underscore'
], function(SavedSearchModel, i18n, _) {

    /**
     * @callback RelatedConceptsClickHandler
     * @param {String[]} newConcepts
     */

    return {
        /**
         * Create a click handler which updates the given query text model.
         * @param {{queryTextModel: *}} options
         * @return {RelatedConceptsClickHandler}
         */
        updateQuery: function(options) {
            return function(newConcepts) {
                var concepts = _.clone(options.queryTextModel.get('relatedConcepts'));
                concepts.push(newConcepts);
                options.queryTextModel.set('relatedConcepts', concepts);
            };
        },

        /**
         * Create a click handler which creates a new query from the current saved search.
         * @param {{savedQueryCollection: *, selectedTabModel: *, savedSearchModel: *}} options
         * @return {RelatedConceptsClickHandler}
         */
        newQuery: function(options) {
            return function(newConcepts) {
                var concepts = _.clone(options.savedSearchModel.get('relatedConcepts'));
                concepts.push(newConcepts);

                var newSearch = new SavedSearchModel(_.defaults({
                    id: null,
                    title: i18n['search.newSearch'],
                    type: SavedSearchModel.Type.QUERY,
                    relatedConcepts: concepts
                }, options.savedSearchModel.attributes));

                options.savedQueryCollection.add(newSearch);
                options.selectedTabModel.set('selectedSearchCid', newSearch.cid);
            };
        }
    };

});
