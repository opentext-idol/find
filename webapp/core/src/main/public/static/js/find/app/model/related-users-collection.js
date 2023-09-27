/*
 * Copyright 2020 Open Text.
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

define([
    'underscore',
    'backbone',
    'find/app/model/find-base-collection'
], function (_, Backbone, FindBaseCollection) {
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
