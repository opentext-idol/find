/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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
    'underscore',
    'jquery',
    'backbone',
    'find/app/configuration',
    'text!find/templates/app/page/search/sort-view.html',
    'i18n!find/nls/bundle'
], function(_, $, Backbone, config, template, i18n) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'click [data-sort]': function(e) {
                const sortType = $(e.currentTarget).attr('data-sort');
                this.queryModel.set('sort', config().search.sortOptions[sortType].sort);
                this.updateCurrentSort(sortType);
            }
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                sortTypes: config().search.sortOptions
            }));

            this.$currentSort = this.$('.current-search-sort');
            this.updateCurrentSort(config().search.defaultSortOption);
        },

        updateCurrentSort: function(sortType) {
            if(this.$currentSort) {
                this.$currentSort.text(
                    config().search.sortOptions[sortType].label ||
                        i18n['search.resultsSort.' + sortType] ||
                        sortType);
            }
        }
    });
});
