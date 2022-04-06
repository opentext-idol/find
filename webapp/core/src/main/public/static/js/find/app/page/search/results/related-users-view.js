/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * List users related to the current search.
 *
 * Options:
 *  - queryModel
 *  - previewModeModel
 *  - config: RelatedUsersConfig
 */

define([
    'underscore',
    'find/app/model/related-users-collection',
    'find/app/page/search/results/users-view'
], function(_, RelatedUsersCollection, UsersView) {
    'use strict';

    const MAX_USERS = 30;

    return UsersView.extend({
        initialize: function (options) {
            this.relatedUsersCollection = new RelatedUsersCollection();
            this.queryModel = options.queryModel;
            this.config = options.config;

            UsersView.prototype.initialize.call(this, _.defaults({
                usersCollection: this.relatedUsersCollection,
                getUserDetailsFields: this.getUserDetailsFields
            }, options));

            this.listenTo(this.queryModel, 'change:queryText', this.update);
        },

        render: function () {
            UsersView.prototype.render.call(this);
            this.update();
        },

        /**
         * @param userModel User being displayed, from relatedUsersCollection
         * @returns Array of UserDetailsFieldConfig
         */
        getUserDetailsFields: function (userModel) {
            return (
                userModel.get('expert') ? this.config.expertise : this.config.interests
            ).userDetailsFields;
        },

        update: function () {
            const queryText = this.queryModel.get('queryText');
            if (!this.$el.is(':visible')) {
                return;
            }

            if (queryText === '' || queryText === '*') {
                this.showNoQuery();
                return;
            }

            this.showLoading();
            this.relatedUsersCollection.fetch({
                reset: true,
                data: { searchText: queryText, maxUsers: MAX_USERS }
            })
        },

    });

});
