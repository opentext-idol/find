/*
 * Copyright 2015-2017 Open Text.
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

const _ = require('underscore');
const DatabasesCollection = require('databases-view/js/idol-databases-collection');

const DatabaseModel = DatabasesCollection.prototype.model;

module.exports = DatabasesCollection.extend({
        url: 'api/public/search/list-indexes',

        fetch: function() {
            const deferred = DatabasesCollection.prototype.fetch.apply(this, arguments);
            this.currentRequest = deferred.promise();
            return deferred;
        },

        parse: function(response) {
            return _.map(response.databases, function(responseItem) {
                responseItem.id = responseItem.name;
                return responseItem;
            });
        },

        model: DatabaseModel.extend({
            defaults: _.extend({
                deleted: false
            }, DatabaseModel.prototype.defaults)
        })
    });

