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

/**
 * Show details about a user in a side panel (see `ResultsViewAugmentation`).
 *
 * Options:
 *  - previewModeModel - attributes:
 *      - user: user model
 *      - fields: array of UserDetailsFieldConfig determining which fields to display
 */

define([
    'underscore',
    'backbone',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/document/preview-mode-user-view.html'
], function(_, Backbone, i18n, template) {
    'use strict';

    const getFieldDisplayName = function (name) {
        return _.map(_.compact(name.split('_')), function (word) {
            return word[0].toUpperCase() + word.substr(1);
        }).join(' ');
    };

    const getPreviewFields = function (userModel, configFields) {
        const previewFields = [];

        if (userModel.get('emailaddress')) {
            previewFields.push({
                name: i18n['search.preview.user.emailFieldName'],
                value: userModel.get('emailaddress')
            });
        }

        previewFields.push({
            name: i18n['search.preview.user.uidFieldName'],
            value: userModel.get('uid')
        });

        const userFields = userModel.get('fields') || {};
        _.each(configFields, function (configField) {
            if (userFields[configField.name]) {
                previewFields.push({
                    name: getFieldDisplayName(configField.name),
                    value: userFields[configField.name]
                });
            }
        });

        return previewFields;
    };

    return Backbone.View.extend({
        className: 'well flex-column m-b-nil full-height preview-mode-user',

        template: _.template(template),

        events: {
            'click .close-preview-mode': 'triggerClose'
        },

        initialize: function (options) {
            this.previewModeModel = options.previewModeModel;
            this.fields = this.previewModeModel.get('fields');
        },

        triggerClose: function () {
            this.previewModeModel.set({ mode: null });
        },

        render: function () {
            this.$el.html(this.template({
                i18n: i18n,
                user: this.model.toJSON(),
                displayFields: getPreviewFields(this.model, this.fields)
            }));
            this.$('.preview-mode-user-loading').addClass('hide');
            this.$('.preview-mode-user-container').removeClass('hide');
        }

    });

});
