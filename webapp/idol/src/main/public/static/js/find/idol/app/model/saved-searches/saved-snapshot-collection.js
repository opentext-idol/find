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
        url: 'api/bi/saved-snapshot',

        model: SavedSearchModel.extend({
            defaults: _.defaults({
                type: SavedSearchModel.Type.SNAPSHOT
            }, SavedSearchModel.prototype.defaults)
        })
    });
});
