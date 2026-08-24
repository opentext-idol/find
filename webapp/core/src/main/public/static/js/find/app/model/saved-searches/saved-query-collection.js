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

const Backbone = require('backbone');
const FindBaseCollection = require('find/app/model/find-base-collection');
const SavedSearchModel = require('find/app/model/saved-searches/saved-search-model');
const _ = require('underscore');

module.exports = FindBaseCollection.extend({
        url: 'api/bi/saved-query',

        parse: function(response) {
            return response.savedQueries;
        },

        model: SavedSearchModel.extend({
            defaults: _.defaults({
                type: SavedSearchModel.Type.QUERY
            }, SavedSearchModel.prototype.defaults)
        })
    });

