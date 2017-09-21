/*
 * Copyright 2014-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/find-base-collection',
    'find/app/model/document-model'
], function(FindBaseCollection, DocumentModel) {
    'use strict';

    return FindBaseCollection.extend({
        model: DocumentModel,
        url: 'api/public/search/query-text-index/results',

        parse: function(response) {
            return response.documents;
        }
    });
});
