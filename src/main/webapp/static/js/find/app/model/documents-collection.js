/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/find-base-collection',
    'find/app/model/document-model',
    'underscore'
], function(FindBaseCollection, DocumentModel, _) {

    return FindBaseCollection.extend({
        model: DocumentModel,
        url: '../api/public/search/query-text-index/results',

        initialize: function(models, options) {
            this.indexesCollection = options.indexesCollection;
        },

        parse: function(response) {
            return _.map(response.documents, function(document) {
                document.index = this.indexesCollection.findWhere({name: document.index});

                return document;
            }, this);
        }
    });

});
