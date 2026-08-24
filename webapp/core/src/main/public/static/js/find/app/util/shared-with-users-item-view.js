/*
 * Copyright 2016 Open Text.
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

const Backbone = require('backbone');
const _ = require('underscore');
const $ = require('jquery');
const i18n = require('find/nls/bundle');
const sharedWithUsersTemplate = require('find/templates/app/util/shared-with-users-item-view.html');

// Loaded for side effects only - do not remove.
require('iCheck');

module.exports = Backbone.View.extend({
        template: _.template(sharedWithUsersTemplate),
        tagName: 'li',
        className: 'flex',

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                user: this.model.get('username'),
                cid: this.model.cid
            }));

            this.$('.js-can-edit-checkbox').iCheck({
                checkboxClass: 'icheckbox-hp clickable'
            });

            this.$('[data-toggle="tooltip"]').tooltip({
                placement: 'top',
                title: i18n['search.savedSearchControl.sharingOptions.unshareWithThisUser'],
            });

            this.updateReadOnly();
        },

        updateReadOnly: function() {
            this.$('.js-can-edit-checkbox').iCheck(this.model.get('canEdit') ? 'check' : 'uncheck');
        }
    });

