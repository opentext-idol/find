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
     * Models have `User` attributes, plus the `expert` attribute from `RelatedUser`.
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
