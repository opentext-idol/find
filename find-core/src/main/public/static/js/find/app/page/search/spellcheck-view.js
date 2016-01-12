/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
define([
    'backbone',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/spellcheck-view.html'
], function(Backbone, i18n, template) {

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

            this.listenTo(this.documentsCollection, 'update', function() {
                var autoCorrection = this.documentsCollection.getAutoCorrection();

                if (autoCorrection) {
                    this.$correctedQuery.text(autoCorrection.correctedQuery);
                    this.$originalQuery.text(autoCorrection.originalQuery);

                    this.show();
                }
                else {
                    this.hide();
                }
            });

            this.listenTo(this.documentsCollection, 'request', this.hide);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.$correctedQuery = this.$('.corrected-query');
            this.$originalQuery = this.$('.original-query');
        },

        hide: function() {
            this.$el.addClass('hidden');
        },

        show: function() {
            this.$el.removeClass('hidden');
        }

    });

});
