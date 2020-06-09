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
