/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    './filter-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/abstract-query-left-side-view.html'
], function(Backbone, _, FilterView, i18n, template) {

    'use strict';

    return Backbone.View.extend({
        // Abstract
        IndexesView: null,

        html: _.template(template)({i18n: i18n}),

        initialize: function(options) {
            this.filterView = new FilterView(_.extend({
                IndexesView: this.IndexesView
            }, options));
        },

        render: function() {
            this.$el.html(this.html);

            this.filterView
                .setElement(this.$('.left-side-filters-view'))
                .render();
        },

        remove: function() {
            this.filterView.remove();
            Backbone.View.prototype.remove.call(this);
        }
    });

});
