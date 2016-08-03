define([
    'backbone',
    'underscore',
    'find/app/util/database-name-resolver',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'find/app/model/document-model',
    'text!find/templates/app/page/search/document/metadata-tab.html'
], function(Backbone, _, databaseNameResolver, i18n, i18nIndexes, DocumentModel, template) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(template),
        
        initialize: function (options) {
            this.indexesCollection = options.indexesCollection
        },

        render: function() {
            //noinspection JSUnresolvedFunction,JSUnresolvedVariable
            this.$el.html(this.template({
                i18n: i18n,
                i18nIndexes: i18nIndexes,
                model: this.model,
                fields: [{
                    key: 'title',
                    value: this.model.get('title')
                },{
                    key: 'url',
                    value: this.model.get('url')
                },{
                    key: 'reference',
                    value: this.model.get('reference')
                },{
                    key: 'index',
                    value: databaseNameResolver.getDatabaseDisplayNameFromDocumentModel(this.indexesCollection, this.model)
                },{
                    key: 'summary',
                    value: this.model.get('summary')
                },{
                    key: 'contentType',
                    value: this.model.get('contentType')
                }]
            }));
        }
    });
});