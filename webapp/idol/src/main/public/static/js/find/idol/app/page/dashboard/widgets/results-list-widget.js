/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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
    'jquery',
    './saved-search-widget',
    'find/app/model/documents-collection',
    'js-whatever/js/list-view',
    'js-whatever/js/list-item-view',
    'text!find/idol/templates/page/dashboards/widgets/results-list-widget-item-view.html'
], function(_, $, SavedSearchWidget, DocumentsCollection, ListView,
            ListItemView, resultsListTemplateItemView) {
    'use strict';

    function hideOverflow() {
        const containerBounds = this.listView.el.getBoundingClientRect();

        this.$('.search-result').each(function(index, element) {
            const boundingClientRect = element.getBoundingClientRect();
            $(element).toggleClass('in-view',
                this.columnLayout
                    ? boundingClientRect.right < containerBounds.right
                    : boundingClientRect.bottom < containerBounds.bottom);
        }.bind(this));
    }

    return SavedSearchWidget.extend({
        viewType: 'list',

        initialize: function(options) {
            SavedSearchWidget.prototype.initialize.apply(this, arguments);

            this.sort = this.widgetSettings.sort || 'relevance';
            this.maxResults = this.widgetSettings.maxResults || 6;
            this.columnLayout = this.widgetSettings.columnLayout;

            this.documentsCollection = new DocumentsCollection();
            this.listView = new ListView({
                className: 'results-list ' + (this.columnLayout
                    ? 'results-list-column'
                    : 'results-list-row'),
                collection: this.documentsCollection,
                ItemView: ListItemView,
                itemOptions: {
                    className: 'search-result',
                    template: _.template(resultsListTemplateItemView)
                }
            });
        },

        render: function() {
            SavedSearchWidget.prototype.render.apply(this);
            this.listView.render();
            this.$content.html(this.listView.$el);
        },

        onResize: function() {
            hideOverflow.call(this);
        },

        updateVisualizer: function() {
            hideOverflow.call(this);
        },

        isEmpty: function() {
            return this.documentsCollection.isEmpty();
        },

        getData: function() {
            return this.documentsCollection
                .fetch({
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

        exportData: function() {
            return {
                data: {
                    drawIcons: false,
                    docs: this.documentsCollection.map(function(model) {
                        return {
                            title: model.get('title'),
                            summary: model.get('summary'),
                            thumbnail: model.get('thumbnail')
                        }
                    })
                },
                type: 'list'
            }
        }
    });
});
