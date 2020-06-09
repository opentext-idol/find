/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
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
        url: 'api/bi/saved-query',

        model: SavedSearchModel.extend({
            defaults: _.defaults({
                type: SavedSearchModel.Type.QUERY
            }, SavedSearchModel.prototype.defaults)
        })
    });
});
