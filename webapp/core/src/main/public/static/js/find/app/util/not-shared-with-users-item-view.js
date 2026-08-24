/*
 * Copyright 2017 Open Text.
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

module.exports = Backbone.View.extend({
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

