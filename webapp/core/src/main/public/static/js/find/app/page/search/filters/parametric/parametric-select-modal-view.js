define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/util/search-data-util',
    'find/app/page/search/filters/parametric/parametric-select-modal-list-view',
    './parametric-paginator',
    'text!find/templates/app/page/search/filters/parametric/parametric-select-modal-view.html',
    'iCheck'
], function (Backbone, $, _, searchDataUtil, ParametricSelectModalListView, ParametricPaginator, template) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(template),
        className: 'full-height',

        events: {
            'click .fields-list a': function (e) {
                e.preventDefault();
                this.fieldSelectionModel.set('field', $(e.currentTarget).closest('[data-field]').attr('data-field'));
            }
        },

        initialize: function (options) {
            const fetchRestrictions = {
                databases: options.queryModel.get('indexes'),
                queryText: options.queryModel.get('autoCorrect') && options.queryModel.get('correctedQuery') ? options.queryModel.get('correctedQuery') : options.queryModel.get('queryText'),
                fieldText: options.queryModel.get('fieldText'),
                minDate: options.queryModel.getIsoDate('minDate'),
                maxDate: options.queryModel.getIsoDate('maxDate'),
                minScore: options.queryModel.get('minScore'),
                stateTokens: options.queryModel.get('stateMatchIds')
            };

            const allIndexes = searchDataUtil.buildIndexes(options.indexesCollection.map(function(model) {
                return model.pick('domain', 'name');
            }));

            this.fieldData = options.parametricFieldsCollection.map(function (fieldModel) {
                const paginator = new ParametricPaginator({
                    fieldName: fieldModel.id,
                    fieldDisplayName: fieldModel.get('displayName'),
                    allIndexes: allIndexes,
                    selectedValues: options.selectedParametricValues,
                    fetchRestrictions: fetchRestrictions,
                    fetchFunction: function (data) {
                        return $.ajax({url: 'api/public/parametric/values', traditional: true, data: data})
                            .then(function (response) {
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
                    view: new ParametricSelectModalListView({
                        showGraphButtons: options.showGraphButtons,
                        paginator: paginator
                    })
                };
            }.bind(this));

            this.fieldSelectionModel = new Backbone.Model({field: options.initialField});
            this.listenTo(this.fieldSelectionModel, 'change', this.updateSelectedField);
        },

        render: function () {
            this.$el.html(this.template({
                initialField: this.initialField,
                fields: _.sortBy(this.fieldData, 'id')
            }));

            const $tabContent = this.$('.tab-content');

            this.fieldData.forEach(function (data) {
                $tabContent.append(data.view.$el);
                data.view.render();
            });

            this.updateSelectedField();
        },

        checkScroll: function() {
            _.findWhere(this.fieldData, {id: this.fieldSelectionModel.get('field')}).view.checkScroll();
        },

        updateSelectedField: function () {
            const currentField = this.fieldSelectionModel.get('field');

            this.$('.fields-list li:not([data-field="' + currentField + '"])').removeClass('active');
            this.$('.fields-list li[data-field="' + currentField + '"]').addClass('active');

            this.fieldData.forEach(function (data) {
                const isCurrentField = data.id === currentField;
                data.view.$el.toggleClass('active', isCurrentField);

                if (isCurrentField) {
                    // Check scroll after showing the view so it can check if more values need to be fetched
                    data.view.checkScroll();
                }
            });
        },

        getSelectedField: function(){
            return this.fieldSelectionModel.get('field');
        },

        remove: function () {
            this.fieldData.forEach(function (data) {
                data.view.remove();
            });

            Backbone.View.prototype.remove.call(this);
        }
    });
});
