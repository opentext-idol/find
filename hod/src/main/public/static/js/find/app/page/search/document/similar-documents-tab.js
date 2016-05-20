/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/page/search/document/abstract-similar-documents-tab'
], function (_, AbstractSimilarDocumentsTab) {

    return AbstractSimilarDocumentsTab.extend({
        getIndexes: function () {
            return _.pluck(this.indexesCollection.where({name: this.model.get('index'), domain: this.model.get('domain')}), 'id');
        }
    });
});