/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/model/find-base-collection',
    'find/app/model/saved-searches/saved-search-model',
    'underscore'
], function(Backbone, FindBaseCollection, SavedSearchModel, _) {
    'use strict';

    return FindBaseCollection.extend({
        url: 'api/bi/saved-snapshot/shared',

        model: SavedSearchModel.extend({
            parse: function(response) {
                const parsedResponse = SavedSearchModel.prototype.parse.call(this, response);

                return _.extend({
                    type: parsedResponse.canEdit ? SavedSearchModel.Type.SHARED_SNAPSHOT : SavedSearchModel.Type.SHARED_READ_ONLY_SNAPSHOT
                }, parsedResponse)
            }
        })
    });
});
