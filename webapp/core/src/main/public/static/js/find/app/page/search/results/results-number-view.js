/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
        },

        getText: function() {
            return $.trim(this.$el.text().replace(/\s+/g, ' '));
        }
    });
});
