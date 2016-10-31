/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/vent',
    'find/app/model/similar-documents-collection',
    'find/app/page/search/document/similar-abstract-tab'
], function(_, vent, SimilarDocumentsCollection, SimilarAbstractTab) {
    'use strict';

    return SimilarAbstractTab.extend({
        events: _.extend({
            'click .similar-documents-tab-see-more': function() {
                vent.navigateToSuggestRoute(this.model);
            }
        }, SimilarAbstractTab.prototype.events),

        // overridden
        getIndexes: _.constant([]),

        createCollection: function() {
            return new SimilarDocumentsCollection();
        },

        fetchData: function () {
            return {
                max_results: 3,
                start: 1,
                summary: 'context',
                indexes: this.getIndexes(),
                reference: this.model.get('reference'),
                highlight: false
            };
        }

    });
});
