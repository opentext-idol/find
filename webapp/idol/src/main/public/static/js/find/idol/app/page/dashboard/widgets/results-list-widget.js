/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    './saved-search-widget',
    'find/app/model/documents-collection',
    'js-whatever/js/list-view',
    'js-whatever/js/list-item-view',
    'text!find/idol/templates/page/dashboards/widgets/results-list-widget-item-view.html'
], function(_, $, SavedSearchWidget, DocumentsCollection, ListView,
            ListItemView, resultsListTemplateItemView) {
    'use strict';

    return SavedSearchWidget.extend({
        viewType: 'list',

        initialize: function(options) {
            SavedSearchWidget.prototype.initialize.apply(this, arguments);
            this.sort = options.widgetSettings.sort || 'relevance';
            this.maxResults = options.widgetSettings.maxResults || 6;
            this.columnLayout = options.widgetSettings.columnLayout;
            this.documentsCollection = new DocumentsCollection();
            this.listView = new ListView({
                className: 'results-list ' + (this.columnLayout ? 'results-list-column' : 'results-list-row'),
                collection: this.documentsCollection,
                ItemView: ListItemView,
                itemOptions: {
                    className: 'search-result',
                    template: _.template(resultsListTemplateItemView)
                }
            });

            this.listenTo(this.documentsCollection, 'update', function() {
                _.defer(_.bind(this.hideOverflow, this));
            });

        },

        render: function() {
            SavedSearchWidget.prototype.render.apply(this);

            this.listView.render();
            this.$content.html(this.listView.$el);
        },

        onResize: function() {
            _.defer(_.bind(this.hideOverflow, this));
        },

        getData: function() {
            return this.documentsCollection.fetch({
                data: {
                    text: this.queryModel.get('queryText'),
                    max_results: this.maxResults,
                    indexes: this.queryModel.get('indexes'),
                    field_text: this.queryModel.get('fieldText'),
                    min_date: this.queryModel.getIsoDate('minDate'),
                    max_date: this.queryModel.getIsoDate('maxDate'),
                    sort: this.sort,
                    summary: 'context',
                    queryType: 'MODIFIED',
                    highlight: false
                },
                reset: false
            });
        },

        hideOverflow: function() {
            const containerBounds = this.listView.el.getBoundingClientRect();

            this.$('.search-result').each(function(index, element) {
                const boundingClientRect = element.getBoundingClientRect();
                $(element).toggleClass('out-of-view',
                    this.columnLayout
                        ? boundingClientRect.right > containerBounds.right
                        : boundingClientRect.bottom > containerBounds.bottom);
            }.bind(this));
        },

        exportPPTData: function(){
            var text = [];

            this.documentsCollection.each(function(model){
                text.push({
                    text: model.get('title') + '\n',
                    fontSize: 11,
                    bold: true
                }, {
                    text: (model.get('title') || '') + '\n\n',
                    fontSize: 10
                })
            });

            return {
                data: {
                    text: text
                },
                type: 'text'
            }
        }
    });
});
