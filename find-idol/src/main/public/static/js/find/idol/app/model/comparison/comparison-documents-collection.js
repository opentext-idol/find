/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/find-base-collection',
    'find/app/model/document-model'
], function(FindBaseCollection, DocumentModel) {

    return FindBaseCollection.extend({
        model: DocumentModel,
        url: '../api/public/comparison/results',

        parse: function(response) {
            this.totalResults = response.totalResults;
            return response.documents;
        }
    });

});
