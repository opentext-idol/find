/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'backbone',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/spellcheck-view.html',
    'underscore'
], function($, Backbone, i18n, template, _) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'click .original-query': function() {
                this.queryModel.set('autoCorrect', false);
            }
        },

        initialize: function(options) {
            this.documentsCollection = options.documentsCollection;
            this.queryModel = options.queryModel;

            this.listenTo(this.documentsCollection, 'request error sync', this.update);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.$correctedQuery = this.$('.corrected-query');
            this.$originalQuery = this.$('.original-query');

            this.update();
        },

        update: function() {
            var autoCorrection = this.documentsCollection.getAutoCorrection();

            if(this.$correctedQuery && autoCorrection) {
                this.queryModel.set('correctedQuery', autoCorrection.correctedQuery);
                this.$correctedQuery.text(autoCorrection.correctedQuery);
                this.$originalQuery.text(autoCorrection.originalQuery);
            }

            this.$el.toggleClass('hidden', !autoCorrection);
        }
    });
});
