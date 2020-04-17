/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * Select a ControlPoint policy and apply it to the documents in a saved search.
 *
 * Dependencies:
 *  - queryState
 *  - savedSearchModel
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/loading-spinner.html',
    'find/app/util/generate-error-support-message',
    'find/app/page/search/results/field-selection-view',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/model/policy-collection',
    'text!find/templates/app/page/search/saved-searches/policy-empty.html'
], function(
    _, $, Backbone, i18n,
    loadingTemplate, generateErrorHtml, FieldSelectionView,
    SavedSearchModel, PolicyCollection, emptyTemplate
) {
    'use strict';

    return Backbone.View.extend({
        loadingTemplate: _.template(loadingTemplate),
        emptyTemplate: _.template(emptyTemplate),

        initialize: function (options) {
            this.queryState = options.queryState;
            this.savedSearchModel = options.savedSearchModel;

            // listed policies
            this.policyCollection = new PolicyCollection();
            // the selected policy - owned by `policySelector`
            this.policySelectionModel = new Backbone.Model();
            // `FieldSelectionView`
            this.policySelector = null;

            this.listenTo(this.policyCollection, 'sync', this.showPolicies);
            this.listenTo(this.policyCollection, 'error', _.partial(this.showError, 'fetch'));
        },

        render: function () {
            this.showLoading();
            this.policyCollection.fetch({ reset: true });
        },

        /**
         * Replace view with a loading indicator.
         */
        showLoading: function () {
            // the listener for this event doesn't get added until after `render` finishes, so in
            // the case when this is called from `render`, we need to delay the event
            const view = this;
            setTimeout(function () {
                view.trigger('primary-button-disable');
            }, 0);

            this.$el.html(this.loadingTemplate({ large: false, i18n: i18n }));
        },

        /**
         * Replace view with a selector for policies in `policyCollection`.
         */
        showPolicies: function () {
            const policies = this.policyCollection.map(function (model) {
                return { id: model.get('id'), displayName: model.get('name') }
            });

            if (policies.length) {
                this.trigger('primary-button-enable');
                this.policySelector = new FieldSelectionView({
                    name: 'policy',
                    fields: policies,
                    model: this.policySelectionModel,
                    allowEmpty: false,
                    width: '50%'
                });
                this.policySelector.render();
                this.$el.html(this.policySelector.$el);

            } else {
                this.$el.html(this.emptyTemplate({ i18n: i18n }));
            }
        },

        /**
         * Replace view with an error message.
         *
         * @param errorId - ID from which to retrieve the error message string, below
         *                  'search.savedSearchControl.applyCPPolicy.error'
         * Remaining arguments form a Collection 'error' event handler.
         */
        showError: function (errorId, _0, xhr) {
            if (xhr.status === 0 && xhr.statusText === 'abort') {
                // canceled
                return;
            }
            this.$el.html(generateErrorHtml({
                errorDetails: xhr.responseJSON && xhr.responseJSON.message,
                errorUUID: xhr.responseJSON && xhr.responseJSON.uuid,
                isUserError: xhr.responseJSON && xhr.responseJSON.isUserError,
                messageToUser: i18n['search.savedSearchControl.applyCPPolicy.error.' + errorId]
            }));
        },

        /**
         * Apply the selected policy to documents matching the search.
         *
         * @param successCallback - Called when the policy has been successfully applied
         */
        applyPolicy: function (successCallback) {
            this.showLoading();

            const params = [
                { name: 'policy', value: this.policySelectionModel.get('field') }
            ];
            let postData = null;
            const searchType = this.savedSearchModel.get('type');
            if (searchType === SavedSearchModel.Type.SNAPSHOT ||
                searchType === SavedSearchModel.Type.READ_ONLY_SNAPSHOT ||
                searchType === SavedSearchModel.Type.SHARED_SNAPSHOT ||
                searchType === SavedSearchModel.Type.SHARED_READ_ONLY_SNAPSHOT
            ) {
                params.push({ name: 'savedSnapshotId', value: this.savedSearchModel.get('id') });
            } else {
                const queryModel = this.savedSearchModel.clone();
                queryModel.set(SavedSearchModel.attributesFromQueryState(this.queryState));
                postData = JSON.stringify(queryModel.toJSON());
            }

            const queryString = _.map(params, function (param) {
                return param.name + '=' + encodeURIComponent(param.value);
            }).join('&');
            $.ajax('api/public/controlpoint/policy/apply?' + queryString, {
                method: 'POST',
                contentType: postData === null ? null : 'application/json',
                data: postData,
                dataType: 'text',
                success: successCallback,
                error: _.bind(this.showError, this, 'apply', null)
            });
        }

    });

});
