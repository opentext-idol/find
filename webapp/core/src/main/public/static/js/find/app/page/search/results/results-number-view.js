/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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
    'jquery',
    'backbone',
    'text!find/templates/app/page/search/results/results-number-view.html',
    'i18n!find/nls/bundle',
    'underscore'
], function($, Backbone, template, i18n, _) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(template),

        initialize: function(options) {
            this.documentsCollection = options.documentsCollection;

            this.listenTo(this.documentsCollection, 'reset update sync', this.updateCounts);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.$currentNumber = this.$('.current-results-number');
            this.$totalNumber = this.$('.total-results-number');
            this.$firstNumber = this.$('.first-result-number');

            this.updateCounts();
        },

        updateCounts: function() {
            if(this.$currentNumber) {
                this.$currentNumber.text(this.documentsCollection.length);
                this.$totalNumber.text(this.documentsCollection.totalResults || 0);
                this.$firstNumber.text(this.documentsCollection.length ? 1 : 0);
            }
        }
    });
});
