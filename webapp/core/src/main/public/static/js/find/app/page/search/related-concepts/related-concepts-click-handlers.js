/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
define([
    'find/app/model/saved-searches/saved-search-model',
    'i18n!find/nls/bundle',
    'underscore'
], function(SavedSearchModel, i18n, _) {

    'use strict';

    /**
     * @callback RelatedConceptsClickHandler
     * @param {String[]} newConcepts
     */

    /**
     * Clone an array of arrays of strings, _.clone does NOT do this.
     * @param {Array.<Array.<string>>} conceptGroups
     * @return {Array.<Array.<string>>}
     */
    function cloneConceptGroups(conceptGroups) {
        return conceptGroups.map(function(concepts) {
            return concepts.map(_.identity);
        });
    }

    function wrapQuotes(concept) {
        return concept ? '"' + concept + '"' : concept;
    }

    return {
        /**
         * Create a click handler which updates the related concept collection
         * @param {{conceptGroups: Backbone.Collection}} options
         * @return {RelatedConceptsClickHandler}
         */
        updateQuery: function(options) {
            return function(newConcepts) {
                options.conceptGroups.push({concepts: newConcepts.map(wrapQuotes)});
            };
        },

        /**
         * Create a click handler which creates a new query from the current saved search.
         * @param {{savedQueryCollection: *, selectedTabModel: *, savedSearchModel: *}} options
         * @return {RelatedConceptsClickHandler}
         */
        newQuery: function(options) {
            return function(newConcepts) {
                const concepts = cloneConceptGroups(options.savedSearchModel.get('relatedConcepts')).concat([newConcepts.map(wrapQuotes)]);

                const newSearch = new SavedSearchModel(_.defaults({
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
