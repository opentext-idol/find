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
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/loading-spinner.html',
    'find/app/util/generate-error-support-message',
    'find/app/page/search/results/field-selection-view',
    'find/app/util/text-input',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/model/controlpoint-policy-collection',
    'find/app/model/nifi-action-collection',
    'text!find/templates/app/page/search/saved-searches/policy-empty.html'
], function(
    _, $, Backbone, configuration, i18n,
    loadingTemplate, generateErrorHtml, FieldSelectionView, TextInput,
    SavedSearchModel, CPPolicyCollection, NifiActionCollection, emptyTemplate
) {
    'use strict';
    // policy implementations
    const IMPL = {
        nifi: 'nifi',
        cp: 'controlpoint'
    }

    return Backbone.View.extend({
        loadingTemplate: _.template(loadingTemplate),
        emptyTemplate: _.template(emptyTemplate),

        initialize: function (options) {
            this.queryState = options.queryState;
            this.savedSearchModel = options.savedSearchModel;
            this.impl = configuration().nifiEnabled ? IMPL.nifi : IMPL.cp

            // listed policies
            this.policyCollection = this.impl === IMPL.nifi ? new NifiActionCollection() :
                this.impl === IMPL.cp ? new CPPolicyCollection() : null;
            // the selected policy - owned by `policySelector`
            this.policySelectionModel = new Backbone.Model();
            // `FieldSelectionView`
            this.policySelector = null;
            // the policy execution label - owned by `policyLabel`
            this.policyLabelModel = new Backbone.Model({ text: '' });
            // `TextInput`
            this.policyLabel = null;

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
                    width: '100%'
                });
                this.policySelector.render();
                this.$el.html(this.policySelector.$el);

                this.policyLabel = new TextInput({
                    model: this.policyLabelModel,
                    modelAttribute: 'text',
                    templateOptions: {
                        placeholder: i18n['search.savedSearchControl.applyPolicy.label.placeholder']
                    }
                });
                this.policyLabel.render();
                this.$el.append(this.policyLabel.$el);

            } else {
                this.$el.html(this.emptyTemplate({ i18n: i18n }));
            }
        },

        /**
         * Replace view with an error message.
         *
         * @param errorId - ID from which to retrieve the error message string, below
         *                  'search.savedSearchControl.applyPolicy.error'
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
                messageToUser: i18n['search.savedSearchControl.applyPolicy.error.' + errorId]
            }));
        },

        /**
         * Apply a policy.
         *
         * @param path Path of the API to call
         * @param params Query parameters to pass to the API
         * @param postData POST body to pass to the API
         * @param successCallback Function to call on success
         */
        applyPolicyBase: function (path, params, postData, successCallback) {
            const queryString = _.map(params, function (param) {
                return param.name + '=' + encodeURIComponent(param.value);
            }).join('&');

            $.ajax(path + '?' + queryString, {
                method: 'POST',
                contentType: postData === null ? null : 'application/json',
                data: postData,
                dataType: 'text',
                success: successCallback,
                error: _.bind(this.showError, this, 'apply', null)
            });
        },

        /**
         * Execute an action using NiFi.
         *
         * @param action ID of the action to execute
         * @param snapshotId Saved snapshot ID, if any
         * @param queryJson Saved search details as JSON, if any
         * @param successCallback Function to call on success
         */
        executeNifiAction: function (action, snapshotId, queryJson, successCallback) {
            const params = [
                { name: 'action', value: action }
            ];
            if (snapshotId) {
                params.push({ name: 'savedSnapshotId', value: snapshotId });
            }
            const title = this.savedSearchModel.get('title');
            if (title) {
                params.push({ name: 'searchName', value: title });
            }
            const label = this.policyLabelModel.get('text');
            if (label) {
                params.push({ name: 'label', value: label });
            }

            const path = 'api/public/nifi/actions/execute';
            this.applyPolicyBase(path, params, queryJson, successCallback);
        },

        /**
         * Apply a policy using ControlPoint.
         *
         * @param action ID of the policy to execute
         * @param snapshotId Saved snapshot ID, if any
         * @param queryJson Saved search details as JSON, if any
         * @param successCallback Function to call on success
         */
        applyCPPolicy: function (policy, snapshotId, queryJson, successCallback) {
            const params = [
                { name: 'policy', value: policy }
            ];
            if (snapshotId) {
                params.push({ name: 'savedSnapshotId', value: snapshotId });
            }

            const path = 'api/public/controlpoint/policy/apply';
            this.applyPolicyBase(path, params, queryJson, successCallback);
        },

        /**
         * Apply the selected policy to documents matching the search.
         *
         * @param successCallback - Called when the policy has been successfully applied
         */
        applyPolicy: function (successCallback) {
            this.showLoading();

            const policy = this.policySelectionModel.get('field');
            let snapshotId = null;
            let queryJson = null;
            const searchType = this.savedSearchModel.get('type');
            if (searchType === SavedSearchModel.Type.SNAPSHOT ||
                searchType === SavedSearchModel.Type.READ_ONLY_SNAPSHOT ||
                searchType === SavedSearchModel.Type.SHARED_SNAPSHOT ||
                searchType === SavedSearchModel.Type.SHARED_READ_ONLY_SNAPSHOT
            ) {
                snapshotId = this.savedSearchModel.get('id');
            } else {
                const queryModel = this.savedSearchModel.clone();
                queryModel.set(SavedSearchModel.attributesFromQueryState(this.queryState));
                queryJson = JSON.stringify(queryModel.toJSON());
            }

            if (this.impl === IMPL.nifi) {
                this.executeNifiAction(policy, snapshotId, queryJson, successCallback);
            } else if (this.impl === IMPL.cp) {
                this.applyCPPolicy(policy, snapshotId, queryJson, successCallback);
            }
        }

    });

});
