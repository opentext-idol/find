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

        initialize: function(models, options) {
            this.text = options.text;
            this.stateMatchIds = options.stateMatchIds;
            this.stateDontMatchIds = options.stateDontMatchIds;
        },

        parse: function(response) {
            this.totalResults = response.totalResults;

            return response.documents;
        },

        fetch: function(options) {
            return FindBaseCollection.prototype.fetch.call(this, _.extend(options || {}, {
                data: _.extend(options.data || {}, {
                    text: this.text,
                    state_match_ids: this.stateMatchIds,
                    state_dont_match_ids: this.stateDontMatchIds || []
                })
            }));
        }
    });

});
