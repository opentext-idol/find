/*
 * Copyright 2018 Open Text.
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
const template = require('find/templates/app/page/search/intent-based-ranking-view.html');
const i18n = require('find/nls/bundle');

module.exports = Backbone.View.extend({
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
                sortTypes: config().search.sortOptions
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

