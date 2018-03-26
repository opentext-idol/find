/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'find/app/model/query-model',
    'text!find/templates/app/page/search/intent-based-ranking-view.html',
    'i18n!find/nls/bundle'
], function(_, $, Backbone, QueryModel, template, i18n) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'change .current-search-intent-based-ranking': function(e) {
                const intentBasedRanking = this.$('.current-search-intent-based-ranking').prop('checked') || false;
                this.queryModel.set('intentBasedRanking', intentBasedRanking);
            }
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;
            this.listenTo(this.queryModel, 'change:intentBasedRanking', this.updateCheckboxState);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                sortTypes: QueryModel.Sort
            }));

            this.$currentSort = this.$('.current-search-intent-based-ranking');
            this.updateCheckboxState();
        },

        updateCheckboxState: function() {
            if(this.$currentSort) {
                this.$currentSort.prop('checked', this.queryModel.get('intentBasedRanking') || false);
            }
        }
    });
});
