define([
    'backbone',
    'underscore',
    'i18n!find/nls/bundle',
    'find/app/model/document-model',
    'text!find/templates/app/page/search/document/metadata-tab.html'
], function(Backbone, _, i18n, DocumentModel, template) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(template),

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                model: this.model
            }));
        }
    });
});