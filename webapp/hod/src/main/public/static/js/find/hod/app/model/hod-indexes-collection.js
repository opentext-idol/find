/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'databases-view/js/hod-databases-collection'
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
                responseItem.id = encodeURIComponent(responseItem.domain) + ':' + encodeURIComponent(responseItem.name);
                if(!responseItem.displayName) {
                    responseItem.displayName = responseItem.name;
                }
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
