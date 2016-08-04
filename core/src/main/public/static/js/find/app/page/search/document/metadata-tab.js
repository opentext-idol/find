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

        events: {
            'click .metadata-tab-see-more': function () {
                //noinspection JSUnresolvedFunction
                this.toggleAdvancedMetadata(true);
            },
            'click .metadata-tab-see-less': function () {
                //noinspection JSUnresolvedFunction
                this.toggleAdvancedMetadata(false);
            }
        },

        toggleAdvancedMetadata: function (showAllMetadata) {
            //noinspection JSUnresolvedFunction
            this.$('.advanced-field').toggleClass('hide', !showAllMetadata);
            //noinspection JSUnresolvedFunction
            this.$('.metadata-tab-see-more').toggleClass('hide', showAllMetadata);
            //noinspection JSUnresolvedFunction
            this.$('.metadata-tab-see-less').toggleClass('hide', !showAllMetadata);
        },
        
        initialize: function (options) {
            //noinspection JSUnresolvedVariable
            this.indexesCollection = options.indexesCollection
        },

        render: function() {
            //noinspection JSUnresolvedFunction
            var importantFields = [{
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
            }];

            //noinspection JSUnresolvedFunction
            var ignoreFields = ['thumbnail', 'DRECONTENT', 'DREDATE', 'DRETITLE', 'DREREFERENCE'].concat(_.pluck(importantFields, 'key'));
            var fields = this.model.get('fields').filter(function (field) {
                //noinspection JSUnresolvedFunction
                return !_.contains(ignoreFields, field.id);
            });
            //noinspection JSUnresolvedFunction
            var partitionedFields = _.partition(fields, function (field) {
                //noinspection JSUnresolvedVariable
                return field.advanced;
            });
            var basicFields = partitionedFields[1];
            var advancedFields = partitionedFields[0];
            //noinspection JSUnresolvedFunction
            this.$el.html(this.template({
                i18n: i18n,
                i18nIndexes: i18nIndexes,
                model: this.model,
                importantFields: importantFields,
                basicFields: basicFields,
                advancedFields: advancedFields
            }));

            if (advancedFields.length === 0) {
                //noinspection JSUnresolvedFunction
                this.$('.metadata-tab-see-more').prop('disabled', true);
            }
        }
    });
});