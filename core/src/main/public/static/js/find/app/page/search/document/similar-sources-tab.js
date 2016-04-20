/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/documents-collection',
    'find/app/page/search/document/similar-abstract-tab'

], function(DocumentsCollection, SimilarAbstractTab) {
    'use strict';

    return SimilarAbstractTab.extend({
        createCollection: function() {
            return new DocumentsCollection([], {
                indexes: this.indexesCollection.pluck('id')
            });
        },

        fetchData: function() {
            return {
                text: '*',
                max_results: 5,
                sort: 'relevance',
                summary: 'context',
                indexes: this.indexesCollection.pluck('id'),
                fieldText: 'MATCH{' + this.model.get('sourceType') + '}:SOURCETYPE'
            }
        }
    });
});
