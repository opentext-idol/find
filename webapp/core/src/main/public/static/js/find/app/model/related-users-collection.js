/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/model/find-base-collection'
], function (Backbone, FindBaseCollection) {
    'use strict';

    /**
     * Users with interest or expertise in a specific document search.
     *
     * Model attributes:
     *  - expert: Whether the user is explicitly tagged as a relevant 'expert', rather than just
     *            having an interest in the topic
     *  - uid
     *  - username
     *  - emailaddress:
     *  - fields: The user's fields as `{ name: value }`
     */
    return FindBaseCollection.extend({
        url: 'api/public/user/related-to-search',

        parse: function (res) {
            return _.map(res, relatedUser => _.defaults(
                { expert: relatedUser.expert },
                relatedUser.user
            ));
        },

        model: Backbone.Model.extend({
            idAttribute: 'uid'
        })

    });
});
