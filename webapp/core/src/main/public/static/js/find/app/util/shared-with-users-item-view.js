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
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'text!find/templates/app/util/shared-with-users-item-view.html',
    'iCheck'
], function(Backbone, _, $, i18n, sharedWithUsersTemplate) {
    'use strict';

    return Backbone.View.extend({
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
});
