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
    'underscore'
], function(Backbone, FindBaseCollection, _) {
    'use strict';

    return FindBaseCollection.extend({
        url: function() {
            return 'api/public/search/shared-searches/permissions/' + this.searchId
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
                    username: response.user.username
                }
            }
        })
    });
});
