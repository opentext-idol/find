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
    'i18n!find/idol/nls/comparisons',
    'text!find/idol/templates/comparison/search-to-compare-view.html'
], function(_, $, Backbone, comparisonsI18n, template) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'click [data-search-cid]': function(e) {
                const $target = $(e.currentTarget);

                this.$('[data-search-cid]').removeClass('selected-saved-search');
                $target.addClass('selected-saved-search');

                this.trigger('selected', $target.attr('data-search-cid'));
            }
        },

        initialize: function(options) {
            this.selectedSearch = options.selectedSearch;
            this.savedSearchCollection = options.savedSearchCollection;
            this.originalSelectedModelCid = options.originalSelectedModelCid;
        },

        render: function() {
            this.$el.html(this.template({
                i18n: comparisonsI18n,
                selectedSearchTitle: this.selectedSearch.get('title'),
                searches: this.savedSearchCollection.reject(function(model) {
                    return model.cid === this.originalSelectedModelCid;
                }, this)
            }));
        }
    });
});
