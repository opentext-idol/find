/*
 * Copyright 2014-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'jquery',
    'find/app/model/saved-searches/saved-search-model',
    'text!find/idol/templates/app/page/dashboards/widget.html'
], function(Backbone, _, $, SavedSearchModel, template) {
    'use strict';

    const DashboardSearchModel = SavedSearchModel.extend({
        urlRoot: 'api/bi/saved-query'
    });


    return Backbone.View.extend({

        template: _.template(template),

        initialize: function(options) {
            this.name = options.name;

            if (options.savedSearchId) {
                this.savedSearchModel = new DashboardSearchModel({
                    id: options.savedSearchId
                });
                this.savedSearchModel.fetch();
            }
        },

        render: function() {
            this.$el.html(this.template({
                name: this.name
            }));

            this.$content = this.$('.content');
        },

        contentHeight: function() {
            return this.$content.height();
        },

        contentWidth: function() {
            return this.$content.width();
        },

        update: $.noop,

        onResize: $.noop

    });

});