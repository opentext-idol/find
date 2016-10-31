/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/document/abstract-similar-documents-tab'
], function (AbstractSimilarDocumentsTab) {

    return AbstractSimilarDocumentsTab.extend({
        getIndexes: function () {
            return  this.indexesCollection.pluck('id');
        }
    });
});