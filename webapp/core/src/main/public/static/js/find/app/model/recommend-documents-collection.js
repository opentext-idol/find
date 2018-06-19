/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/model/find-base-collection',
    'find/app/model/document-model'
], function(_, BaseCollection, DocumentModel) {
    'use strict';

    return BaseCollection.extend({
        url: 'api/public/search/recommend-documents',
        model: DocumentModel,

        parse: function(response) {
            this.totalResults = response.totalResults;
            return response.documents;
        }
    });
});
