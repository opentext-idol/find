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
    'jquery',
    'backbone',
    'find/app/util/search-data-util',
    'find/app/page/search/filters/parametric/parametric-select-modal-list-view',
    './parametric-paginator',
    'text!find/templates/app/page/search/filters/parametric/parametric-select-modal-view.html',
    'iCheck'
], function(_, $, Backbone, searchDataUtil, ParametricSelectModalListView, ParametricPaginator, template) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(template),
        className: 'full-height',

        events: {
            'click .fields-list a': function(e) {
                e.preventDefault();
                this.fieldSelectionModel.set('field', $(e.currentTarget).closest('[data-field]').attr('data-field'));
            }
        },

        initialize: function(options) {
            const fetchRestrictions = {
                databases: options.queryModel.get('indexes'),
                queryText: options.queryModel.get('autoCorrect') && options.queryModel.get('correctedQuery')
                    ? options.queryModel.get('correctedQuery')
                    : options.queryModel.get('queryText'),
                fieldText: options.queryModel.get('fieldText'),
                minDate: options.queryModel.getIsoDate('minDate'),
                maxDate: options.queryModel.getIsoDate('maxDate'),
                minScore: options.queryModel.get('minScore'),
                stateTokens: options.queryModel.get('stateMatchIds')
            };

            const allIndexes = searchDataUtil.buildIndexes(options.indexesCollection.map(function(model) {
                return model.pick('domain', 'name');
            }));

            this.fieldData = options.parametricFieldsCollection.where({type: 'Parametric'}).map(function(fieldModel) {
                const paginator = new ParametricPaginator({
                    fieldName: fieldModel.id,
                    fieldDisplayName: fieldModel.get('displayName'),
                    allIndexes: allIndexes,
                    selectedValues: options.selectedParametricValues,
                    // Use 200 instead of 20 since most of the cost is in processing the query, not returning the results
                    pageSize: 200,
                    fetchRestrictions: fetchRestrictions,
                    fetchFunction: function(origData) {
                        const data = _.extend({
                            sort: options.parametricValuesSort
                        }, origData);

                        return $.ajax({url: 'api/public/parametric/values', traditional: true, data: data})
                            .then(function(response) {
                                return {
                                    totalValues: response[0] ? response[0].totalValues : 0,
                                    values: response[0] ? response[0].values : []
                                };
                            });
                    }
                });

                return {
                    id: fieldModel.id,
                    displayName: fieldModel.get('displayName'),
                    view: new ParametricSelectModalListView({paginator: paginator})
                };
            }.bind(this));

            this.fieldSelectionModel = new Backbone.Model({field: options.initialField});
            this.listenTo(this.fieldSelectionModel, 'change', this.updateSelectedField);
        },

        render: function() {
            this.$el.html(this.template({
                initialField: this.initialField,
                fields: this.fieldData
            }));

            const $tabContent = this.$('.tab-content');

            this.fieldData.forEach(function(data) {
                $tabContent.append(data.view.$el);
                data.view.render();
            });

            this.updateSelectedField();

            this.$el.tooltip({
                selector: '[data-toggle="tooltip"]',
                container: this.$el
            })
        },

        checkScroll: function() {
            _.findWhere(this.fieldData, {id: this.fieldSelectionModel.get('field')}).view.checkScroll();
        },

        updateSelectedField: function() {
            const currentField = this.fieldSelectionModel.get('field');

            this.$('.fields-list li:not([data-field="' + currentField + '"])').removeClass('active');
            this.$('.fields-list li[data-field="' + currentField + '"]').addClass('active');

            this.fieldData.forEach(function(data) {
                const isCurrentField = data.id === currentField;
                data.view.$el.toggleClass('active', isCurrentField);

                if(isCurrentField) {
                    // Check scroll after showing the view so it can check if more values need to be fetched
                    data.view.checkScroll();
                }
            });
        },

        remove: function() {
            this.fieldData.forEach(function(data) {
                data.view.remove();
            });

            Backbone.View.prototype.remove.call(this);
        }
    });
});
