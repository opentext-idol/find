/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
