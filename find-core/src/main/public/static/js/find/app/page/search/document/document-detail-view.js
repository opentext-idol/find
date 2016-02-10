/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/vent',
    'i18n!find/nls/bundle',
    'find/app/util/view-server-client',
    'text!find/templates/app/page/search/document/document-detail.html',
    'text!find/templates/app/page/view/media-player.html'
], function(Backbone, vent, i18n, viewClient, template, mediaTemplate) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(template),
        mediaTemplate: _.template(mediaTemplate),

        className: 'row flex-container',

        events: {
            'click .service-view-back-button': function() {
                vent.navigate(this.backUrl);
            }
        },

        initialize: function(options) {
            this.model = options.model;
            this.backUrl = options.backUrl;
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));
        }
    });
});
