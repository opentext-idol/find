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
        url: function() {
            return '/api/public/search/shared-searches/permissions/' + this.searchId
        },

        initialize: function(models, options) {
            this.searchId = options.searchId;
        },

        model: Backbone.Model.extend({

            idAttribute: 'username',

            url: function() {
                let url = _.result(this.collection, 'url');

                if (this.get('userId')) {
                    url +=  '/' + this.get('userId');
                }

                return url;
            },

            // these models have a composite id on the server
            isNew: function() {
                return !this.has('userId')
            },

            parse: function(response) {
                return {
                    canEdit: response.canEdit,
                    userId: response.id.userId,
                    searchId: response.id.searchId,
                    modifiedDate: response.modifiedDate,
                    username: response.user.username
                }
            }
        })
    });
});