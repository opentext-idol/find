/*
 * Copyright 2014-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'jquery',
    'find/app/vent',
    'find/app/model/saved-searches/saved-search-model',
    'text!find/idol/templates/app/page/dashboards/widget.html'
], function(Backbone, _, $, vent, SavedSearchModel, template) {
    'use strict';

    const DashboardSearchModel = SavedSearchModel.extend({
        urlRoot: function() {
            return 'api/bi/' + (this.get('type') === 'QUERY' ? 'saved-query': 'saved-snapshot');
        }
    });


    return Backbone.View.extend({

        viewType: '',

        clickable: false,

        template: _.template(template),

        initialize: function(options) {
            this.name = options.name;

            if (options.savedSearch) {
                if (this.clickable) {
                    this.savedSearchRoute = '/search/tab/' + options.savedSearch.type + ':' + options.savedSearch.id + (this.viewType ? '/view/' + this.viewType : '');
                }
                this.savedSearchModel = new DashboardSearchModel({
                    id: options.savedSearch.id,
                    type: options.savedSearch.type
                });
                this.fetchPromise = this.savedSearchModel.fetch();
            }
        },

        render: function() {
            this.$el.html(this.template({
                name: this.name
            }));

            if (this.clickable) {
                this.$el.click(function () {
                    if (this.savedSearchRoute) {
                        vent.navigate(this.savedSearchRoute);
                    }
                }.bind(this));
            }

            this.$content = this.$('.content');
        },

        contentHeight: function() {
            return this.$content.height();
        },

        contentWidth: function() {
            return this.$content.width();
        },

        isUpdating: _.constant(false),

        onResize: $.noop

    });

});