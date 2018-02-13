/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/model/find-base-collection',
    'underscore'
], function(Backbone, FindBaseCollection, _) {
    'use strict';

    return FindBaseCollection.extend({
        url: 'api/bi/user/search',
        parse: function(response) {
            return response.user;
        },

        model: Backbone.Model.extend({
            idAttribute: 'username'
        })
    });
});