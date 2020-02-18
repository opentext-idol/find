/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/model/find-base-collection',
    'find/app/model/document-model'
], function(_, FindBaseCollection, DocumentModel) {
    'use strict';

    return FindBaseCollection.extend({
        url: 'api/public/answer/entity-facts'
    });
});
