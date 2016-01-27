define([
    'backbone',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'find/app/model/document-model',
    'text!find/templates/app/page/search/preview-mode-view.html',
    'text!find/templates/app/page/search/preview-mode-metadata.html'
], function(Backbone, _, $, i18n, DocumentModel, template, metaDataTemplate) {
    "use strict";

    return Backbone.View.extend({
        template: _.template(template),
        metaDataTemplate: _.template(metaDataTemplate),

        render: function() {
            this.$el.html(this.template({
                i18n:i18n
            }));
        },

        renderView: function(model) {
            this.$('.preview-mode-metadata').html(this.metaDataTemplate({
                i18n:i18n,
                model: model,
                arrayFields: DocumentModel.ARRAY_FIELDS,
                dateFields: DocumentModel.DATE_FIELDS,
                fields: ['index', 'reference', 'contentType', 'url']
            }));
        }
    });

});
