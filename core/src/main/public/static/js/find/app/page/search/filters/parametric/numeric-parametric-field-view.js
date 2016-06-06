/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/page/search/filters/parametric/numeric-widget',
    'parametric-refinement/prettify-field-name',
    'parametric-refinement/selected-values-collection',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filters/parametric/numeric-parametric-field-view.html'
], function (Backbone, $, _, numericWidget, prettifyFieldName, SelectedParametricValuesCollection, i18n, template) {
    "use strict";
    const GRAPH_HEIGHT = 110;

    function resetSelectedParametricValues(selectedParametricValues, fieldName) {
        const existingRestrictions = selectedParametricValues.where({field: fieldName});
        existingRestrictions.forEach(function (model) {
            selectedParametricValues.remove(model);
        });
    }

    function updateRestrictions(selectedParametricValues, fieldName, min, max) {
        selectedParametricValues.add({
            field: fieldName,
            range: [min, max],
            numeric: true
        });
    }
    
    function updateModel(model, queryModel, targetNumberOfBuckets, newMin, newMax) {
        $.ajax({
                url: '../api/public/parametric/buckets',
                traditional: true,
                data: {
                    fieldNames: [model.get('id')],
                    databases: queryModel.get('indexes'),
                    queryText: queryModel.get('queryText'),
                    targetNumberOfBuckets: [targetNumberOfBuckets],
                    bucketMin: [newMin],
                    bucketMax: [newMax]
                }
            })
            .success(function (results) {
                const result = results[0];
                model.set({
                    count: result.count,
                    min: result.min,
                    max: result.max,
                    bucketSize: result.bucketSize,
                    values: result.values
                });
            });
    }

    function roundInputNumber(x1) {
        return Math.round(x1 * 10) / 10;
    }

    return Backbone.View.extend({
        className: 'animated fadeIn',
        template: _.template(template),

        events: {
            'click .numeric-parametric-no-min': function () {
                //noinspection JSUnresolvedVariable
                const $minInput = this.$minInput;
                //noinspection JSUnresolvedVariable
                const absoluteMinValue = this.absoluteMinValue;
                if (absoluteMinValue !== $minInput.val()) {
                    $minInput.val(absoluteMinValue);
                    $minInput.trigger('change');
                }
            },
            'click .numeric-parametric-no-max': function () {
                //noinspection JSUnresolvedVariable
                const $maxInput = this.$maxInput;
                //noinspection JSUnresolvedVariable
                const absoluteMaxValue = this.absoluteMaxValue;
                if (absoluteMaxValue !== $maxInput.val()) {
                    $maxInput.val(absoluteMaxValue);
                    $maxInput.trigger('change');
                }
            },
            'click .numeric-parametric-reset': function () {
                //noinspection JSUnresolvedVariable
                resetSelectedParametricValues(this.selectedParametricValues, this.fieldName);
                //noinspection JSUnresolvedVariable
                updateModel(this.model, this.queryModel, Math.floor(this.$el.width() / this.pixelsPerBucket), this.absoluteMinValue, this.absoluteMaxValue);
            },
            'change .numeric-parametric-min-input': function () {
                //noinspection JSUnresolvedVariable
                updateRestrictions(this.selectedParametricValues, this.fieldName, this.$minInput.val(), this.$maxInput.val());
                //noinspection JSUnresolvedFunction
                this.drawSelection();
            },
            'change .numeric-parametric-max-input': function () {
                //noinspection JSUnresolvedVariable
                updateRestrictions(this.selectedParametricValues, this.fieldName, this.$minInput.val(), this.$maxInput.val());
                //noinspection JSUnresolvedFunction
                this.drawSelection();
            }
        },

        initialize: function (options) {
            this.queryModel = options.queryModel;
            this.selectedParametricValues = options.selectedParametricValues;
            this.pixelsPerBucket = options.pixelsPerBucket;
            this.viewWidth = options.viewWidth;
            this.fieldName = this.model.id;
            this.widget = numericWidget({});

            this.absoluteMinValue = this.model.get('min');
            this.absoluteMaxValue = this.model.get('max');
        },

        render: function () {
            //noinspection JSUnresolvedVariable,JSUnresolvedFunction
            this.$el.empty().append(this.template({
                i18n: i18n,
                fieldName: prettifyFieldName(this.model.get('name')),
                id: _.uniqueId('numeric-parametric-field')
            }));

            //noinspection JSUnresolvedFunction
            this.$minInput = this.$('.numeric-parametric-min-input');
            //noinspection JSUnresolvedFunction
            this.$maxInput = this.$('.numeric-parametric-max-input');

            this.$minInput.val(this.absoluteMinValue);
            this.$maxInput.val(this.absoluteMaxValue);

            const updateCallback = function (x1, x2) {
                // rounding to one decimal place
                this.$minInput.val(Math.max(roundInputNumber(x1), this.model.get('min')));
                this.$maxInput.val(Math.min(roundInputNumber(x2), this.model.get('max')));
            }.bind(this);
            const selectionCallback = function (x1, x2) {
                updateRestrictions(this.selectedParametricValues, this.fieldName, Math.max(x1, this.model.get('max')), Math.min(x2, this.model.get('min')));
            }.bind(this);
            const deselectionCallback = function () {
                this.$minInput.val(this.absoluteMinValue);
                this.$maxInput.val(this.absoluteMaxValue);
                resetSelectedParametricValues(this.selectedParametricValues, this.fieldName);
            }.bind(this);
            const zoomCallback = function (newMin, newMax) {
                //noinspection JSUnresolvedVariable
                updateModel(this.model, this.queryModel, Math.floor(this.$el.width() / this.pixelsPerBucket), newMin, newMax);
            }.bind(this);
            const buckets = this.model.get('values');
            //noinspection JSUnresolvedFunction
            this.graph = this.widget.drawGraph({
                chart: this.$('.chart')[0],
                data: {
                    buckets: buckets,
                    bucketSize: this.model.get('bucketSize'),
                    maxCount: _.max(_.pluck(buckets, 'count')),
                    minValue: this.model.get('min'),
                    maxValue: this.model.get('max')
                },
                updateCallback: updateCallback,
                selectionCallback: selectionCallback,
                deselectionCallback: deselectionCallback,
                zoomCallback: zoomCallback,
                xRange: this.viewWidth,
                yRange: GRAPH_HEIGHT,
                tooltip: i18n['search.numericParametricFields.tooltip']
            });

            this.drawSelection();
        },
        
        drawSelection: function () {
            const graph = this.graph;
            
            this.selectedParametricValues.where({
                field: this.fieldName
            }).forEach(function (restriction) {
                const range = restriction.get('range');
                if (range) {
                    this.$minInput.val(roundInputNumber(range[0]));
                    this.$maxInput.val(roundInputNumber(range[1]));

                    graph.selectionRect.init(graph.chart, GRAPH_HEIGHT, graph.scale.barWidth(range[0]));
                    graph.selectionRect.update(graph.scale.barWidth(range[1]));
                    graph.selectionRect.focus();
                }
            }, this);
        }
    });
});