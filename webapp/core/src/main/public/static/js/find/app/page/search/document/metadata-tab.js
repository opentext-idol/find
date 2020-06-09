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
    'backbone',
    'find/app/util/database-name-resolver',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'find/app/model/document-model',
    'text!find/templates/app/page/search/document/metadata-tab.html'
], function(_, Backbone, databaseNameResolver, i18n, i18nIndexes, DocumentModel, template) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'click .metadata-tab-see-more': function() {
                if(!this.$('.metadata-tab-see-more').hasClass('disabled')) {
                    this.toggleAdvancedMetadata(true);
                }
            },
            'click .metadata-tab-see-less': function() {
                this.toggleAdvancedMetadata(false);
            }
        },

        toggleAdvancedMetadata: function(showAllMetadata) {
            this.$('.advanced-field').toggleClass('hide', !showAllMetadata);
            this.$('.metadata-tab-see-more').toggleClass('hide', showAllMetadata);
            this.$('.metadata-tab-see-less').toggleClass('hide', !showAllMetadata);
        },

        initialize: function(options) {
            this.indexesCollection = options.indexesCollection
        },

        render: function() {
            const importantFields = [{
                key: 'title',
                value: this.model.get('title')
            }, {
                key: 'url',
                value: this.model.get('url')
            }, {
                key: 'reference',
                value: this.model.get('reference')
            }, {
                key: 'index',
                value: databaseNameResolver.getDatabaseDisplayNameFromDocumentModel(this.indexesCollection, this.model)
            }, {
                key: 'summary',
                value: this.model.get('summary')
            }, {
                key: 'contentType',
                value: this.model.get('contentType')
            }];

            const ignoreFields = ['thumbnail', 'DRECONTENT', 'DREDATE', 'DRETITLE', 'DREREFERENCE']
                .concat(_.pluck(importantFields, 'key'));
            const fields = this.model.get('fields').filter(function(field) {
                return !_.contains(ignoreFields, field.id);
            });

            const partitionedFields = _.partition(fields, function(field) {
                return field.advanced;
            });

            this.$el.html(this.template({
                i18n: i18n,
                i18nIndexes: i18nIndexes,
                model: this.model,
                importantFields: importantFields,
                basicFields: partitionedFields[1],
                advancedFields: partitionedFields[0]
            }));

            // If there are no advanced fields
            if(partitionedFields[0].length === 0) {
                this.$('.metadata-tab-see-more')
                    .tooltip('destroy')
                    .toggleClass('disabled', true)
                    .tooltip({
                        placement: 'top',
                        title: i18n['search.document.detail.tabs.metadata.noAdvanced'],
                        container: 'body'
                    });
            }
        },

        remove: function() {
            this.$('.metadata-tab-see-more').tooltip('destroy');
            Backbone.View.prototype.remove.call(this);
        }
    });
});
