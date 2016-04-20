/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'databases-view/js/databases-collection',
    'underscore'
], function (DatabasesCollection, _) {

    var DatabaseModel = DatabasesCollection.prototype.model;

    return DatabasesCollection.extend({
        url: '../api/public/search/list-indexes',

        parse: function (response) {
            return _.map(response, function (responseItem) {
                responseItem.id = responseItem.domain ? encodeURIComponent(responseItem.domain) + ':' + encodeURIComponent(responseItem.name) : responseItem.name;
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
