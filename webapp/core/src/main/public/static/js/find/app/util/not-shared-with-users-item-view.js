/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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
    'i18n!find/nls/bundle'
], function(Backbone, _, $, i18n) {
    'use strict';

    return Backbone.View.extend({
        template: _.template('<i class="fa fa-share-alt m-r-sm js-share-with-button clickable" data-toggle="tooltip" data-username="<%-user%>"></i><span><%-user%></span>'),
        tagName: 'li',

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                user: this.model.get('username')
            }));

            this.$('[data-toggle="tooltip"]').tooltip({
                placement: 'top',
                title: i18n['search.savedSearchControl.sharingOptions.shareWithThisUser'],
            });
        }
    });
});
