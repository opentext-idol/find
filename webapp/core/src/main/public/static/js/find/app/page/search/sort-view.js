/*
 * Copyright 2016-2017 Open Text.
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

const _ = require('underscore');
const $ = require('jquery');
const Backbone = require('backbone');
const config = require('find/app/configuration');
const template = require('find/templates/app/page/search/sort-view.html');
const i18n = require('find/nls/bundle');

module.exports = Backbone.View.extend({
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

