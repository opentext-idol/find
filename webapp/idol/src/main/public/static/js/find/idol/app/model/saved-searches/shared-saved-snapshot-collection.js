/*
 * Copyright 2016 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
