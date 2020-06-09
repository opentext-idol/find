/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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
    'underscore',
    'databases-view/js/idol-databases-collection'
], function(_, DatabasesCollection) {
    'use strict';

    const DatabaseModel = DatabasesCollection.prototype.model;

    return DatabasesCollection.extend({
        url: 'api/public/search/list-indexes',

        fetch: function() {
            const deferred = DatabasesCollection.prototype.fetch.apply(this, arguments);
            this.currentRequest = deferred.promise();
            return deferred;
        },

        parse: function(response) {
            return _.map(response, function(responseItem) {
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
});
