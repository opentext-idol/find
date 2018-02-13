/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/model/find-base-collection',
    'find/app/model/document-model'
], function(_, FindBaseCollection, DocumentModel) {
    'use strict';

    return FindBaseCollection.extend({
        model: DocumentModel,
        url: 'api/public/entitysearch/search'
    });
});
