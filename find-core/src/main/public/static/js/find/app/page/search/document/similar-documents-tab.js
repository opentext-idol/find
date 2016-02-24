/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/similar-documents-collection',
    'find/app/page/search/document/similar-abstract-tab'

], function(SimilarDocumentsCollection, SimilarAbstractTab) {
    'use strict';

    return SimilarAbstractTab.extend({
        createCollection: function() {
            return new SimilarDocumentsCollection([], {
                indexes: this.indexesCollection.pluck('id'),
                reference: this.model.get('reference')
            });
        },

        fetchData: function() { return {} }
    });
});
