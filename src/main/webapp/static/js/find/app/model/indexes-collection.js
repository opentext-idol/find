/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone'
], function(Backbone) {

    return Backbone.Collection.extend({

        url: '../api/search/list-indexes',

        parse: function(response) {
            return _.map(response, function(responseItem) {
                responseItem.id = encodeURIComponent(responseItem.domain) + ':' + encodeURIComponent(responseItem.name);

                return responseItem;
            });
        }

    })

});
