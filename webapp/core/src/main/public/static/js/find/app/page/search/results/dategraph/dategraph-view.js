/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
define([
    'backbone',
    'underscore',
    'find/app/util/topic-map-view',
    'find/app/model/entity-collection',
    'i18n!find/nls/bundle',
    'find/app/configuration',
    'find/app/util/generate-error-support-message',
    'text!find/templates/app/page/search/results/dategraph/dategraph-view.html',
    'text!find/templates/app/page/loading-spinner.html',
    'iCheck',
    'slider/bootstrap-slider'
], function(Backbone, _, TopicMapView, EntityCollection, i18n, configuration, generateErrorHtml, template,
            loadingTemplate) {
    'use strict';

    var loadingHtml = _.template(loadingTemplate)({i18n: i18n, large: true});

    return Backbone.View.extend({
        template: _.template(template),

        events: {
        },

        initialize: function(options) {
            this.queryState = options.queryState;

        },

        update: function() {
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                errorTemplate: this.errorTemplate,
                loadingHtml: loadingHtml,
                cid: this.cid
            }));
        }
    });
});
