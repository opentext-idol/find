/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/model/find-base-collection',
    'find/app/model/document-model'
], function(_, BaseCollection, DocumentModel) {

    return BaseCollection.extend({
        url: '../api/public/search/similar-documents',
        model: DocumentModel,

        parse: function(response) {
            return response.documents;
        },

        initialize: function(models, options) {
            this.indexes = options.indexes;
            this.reference = options.reference;
        },

        fetch: function(options) {
            return BaseCollection.prototype.fetch.call(this, _.extend(options || {}, {
                data: {
                    indexes: this.indexes,
                    reference: this.reference
                }
            }));
        }
    });

});