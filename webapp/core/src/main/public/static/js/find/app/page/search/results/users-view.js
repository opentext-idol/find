/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * List users.
 *
 * Options:
 *  - previewModeModel
 *  - usersCollection: a collection of the users to display
 *                    (com.hp.autonomy.types.idol.responses.User)
 *  - getUserDetailsFields: function taking a user model from `usersCollection`, and returning an
 *                          array of UserDetailsFieldConfig
 *
 * To be used with `ResultsViewAugmentation` (the `previewModeModel` collection is used to signal
 * that a user preview should be opened).
 */

define([
    'underscore',
    'backbone',
    'jquery',
    'i18n!find/nls/bundle',
    'find/app/util/generate-error-support-message',
    'text!find/templates/app/page/search/results/users-view.html',
    'text!find/templates/app/page/search/results/users-view-user.html'
], function(
    _, Backbone, $, i18n, generateErrorHtml, template, userTemplate
) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(template),
        userTemplate: _.template(userTemplate),

        initialize: function (options) {
            this.previewModeModel = options.previewModeModel;
            this.usersCollection = options.usersCollection;
            this.getUserDetailsFields = options.getUserDetailsFields;

            this.listenTo(this.usersCollection, 'sync', this.showUsers);
            this.listenTo(this.usersCollection, 'error', this.showUsersError);
            this.listenTo(this.previewModeModel, 'change', this.updateSelectedUser);
        },

        events: {
            'click .users-list [data-uid]': function (e) {
                const $user = $(e.currentTarget);
                if ($user.hasClass('selected-document')) {
                    this.previewModeModel.set({ mode: null });
                } else {
                    this.showUserPreview($user);
                }
            }
        },

        render: function () {
            this.$el.html(this.template({ i18n: i18n }));
        },

        /**
         * Highlight the user whose details are currently shown, if any.
         */
        updateSelectedUser: function () {
            this.$('.users-list [data-uid]').removeClass('selected-document');
            if (this.previewModeModel.get('mode') === 'user') {
                const selectedUser = this.previewModeModel.get('user');
                if (selectedUser) {
                    this.$('.users-list [data-uid="' + selectedUser.get('uid') + '"]')
                        .addClass('selected-document');
                }
            }
        },

        /**
         * Hide all optional elements of the view.
         */
        resetView: function () {
            this.$('.users-loading').addClass('hide');
            this.$('.users-error').addClass('hide');
            this.$('.users-empty').addClass('hide');
            this.$('.users-noquery').addClass('hide');
            this.$('.users-list').addClass('hide');
        },

        /**
         * Show only an indicator that users are being retrieved.
         */
        showLoading: function () {
            this.resetView();
            this.$('.users-loading').removeClass('hide');
        },

        /**
         * Show only a message indicating that there's no query text to use.
         */
        showNoQuery: function () {
            this.resetView();
            this.$('.users-noquery').removeClass('hide');
        },

        /**
         * Show an error because a request failed.
         *
         * @param xhr - request object
         * @param errorId - ID from which to retrieve the error message string, below
         *                  'search.resultsView.users.error'
         */
        showError: function (xhr, errorId) {
            if (xhr.status === 0 && xhr.statusText === 'abort') {
                return;
            }

            this.resetView();
            this.$('.users-error').html(generateErrorHtml({
                errorDetails: xhr.responseJSON && xhr.responseJSON.message,
                errorUUID: xhr.responseJSON && xhr.responseJSON.uuid,
                isUserError: xhr.responseJSON && xhr.responseJSON.isUserError,
                messageToUser: i18n['search.resultsView.users.error.' + errorId]
            }));
            this.$('.users-error').removeClass('hide');
        },

        /**
         * Show an error because retrieving users failed.  Collection 'error' event handler.
         */
        showUsersError: function (_0, xhr) {
            this.showError(xhr, 'fetchUsers');
        },

        /**
         * Show users in `usersCollection`.
         */
        showUsers: function () {
            this.resetView();

            if (this.usersCollection.length === 0) {
                this.$('.users-empty').removeClass('hide');

            } else {
                const usersList = this.$('.users-list');
                usersList.html('');
                this.usersCollection.each(_.bind(function (userModel) {
                    usersList.append(this.userTemplate({
                        user: userModel.toJSON()
                    }));
                }, this));
                this.$('.users-list').removeClass('hide');
            }
        },

        /**
         * Show a preview for a user.
         *
         * @param $user Element shown in the user list for this user
         */
        showUserPreview: function ($user) {
            const userModel = this.usersCollection.findWhere({ uid: $user.data('uid') });
            if (userModel) {
                this.$('.users-error').addClass('hide');
                this.previewModeModel.set({
                    mode: 'user',
                    user: userModel,
                    fields: this.getUserDetailsFields(userModel)
                });
                // keep the clicked user visible
                $user[0].scrollIntoView({ behavior: 'smooth', block: 'nearest' });
            }
        }

    });

});
