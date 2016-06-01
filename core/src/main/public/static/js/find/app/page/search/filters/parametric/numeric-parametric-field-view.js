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
                //noinspection JSUnresolvedFunction
                this.executeCallbackWithoutRestrictions(function (result) {
                    //noinspection JSUnresolvedFunction
                    const minValue = roundInputNumber(_.first(result.values).value);
                    if (minValue !== $minInput.val()) {
                        $minInput.val(minValue);
                        $minInput.trigger('change');
                    }
                });
            },
            'click .numeric-parametric-no-max': function () {
                //noinspection JSUnresolvedVariable
                const $maxInput = this.$maxInput;
                //noinspection JSUnresolvedFunction
                this.executeCallbackWithoutRestrictions(function (result) {
                    //noinspection JSUnresolvedFunction
                    const maxValue = roundInputNumber(_.last(result.values).value);
                    if (maxValue !== $maxInput.val()) {
                        $maxInput.val(maxValue);
                        $maxInput.trigger('change');
                    }
                });
            },
            'click .numeric-parametric-reset': function () {
                //noinspection JSUnresolvedVariable
                resetSelectedParametricValues(this.selectedParametricValues, this.fieldName);
            },
            'change .numeric-parametric-min-input': function () {
                //noinspection JSUnresolvedVariable
                updateRestrictions(this.selectedParametricValues, this.fieldName, this.$minInput.val(), this.$maxInput.val());
            },
            'change .numeric-parametric-max-input': function () {
                //noinspection JSUnresolvedVariable
                updateRestrictions(this.selectedParametricValues, this.fieldName, this.$minInput.val(), this.$maxInput.val());
            }
        },

        initialize: function (options) {
            this.queryModel = options.queryModel;
            this.selectedParametricValues = options.selectedParametricValues;
            this.viewWidth = options.viewWidth;
            this.fieldName = this.model.id;
            this.widget = numericWidget({
                graphHeight: GRAPH_HEIGHT
            });
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

            const minValue = this.model.get('min');
            const maxValue = this.model.get('max');
            this.$minInput.val(minValue);
            this.$maxInput.val(maxValue);

            //noinspection JSUnresolvedFunction
            const updateCallback = _.bind(function (x1, x2) {
                // rounding to one decimal place
                this.$minInput.val(roundInputNumber(x1));
                this.$maxInput.val(roundInputNumber(x2));
            }, this);
            //noinspection JSUnresolvedFunction
            const selectionCallback = _.bind(function (x1, x2) {
                updateRestrictions(this.selectedParametricValues, this.fieldName, x1, x2);
            }, this);
            //noinspection JSUnresolvedFunction
            const deselectionCallback = _.bind(function () {
                this.$minInput.val(minValue);
                this.$maxInput.val(maxValue);
                resetSelectedParametricValues(this.selectedParametricValues, this.fieldName);
            }, this);
            const buckets = this.model.get('values');
            //noinspection JSUnresolvedFunction
            const graph = this.widget.drawGraph({
                chart: this.$('.chart')[0],
                data: {
                    buckets: buckets,
                    bucketSize: this.model.get('bucketSize'),
                    maxCount: _.max(_.pluck(buckets, 'count'))
                },
                updateCallback: updateCallback,
                selectionCallback: selectionCallback,
                deselectionCallback: deselectionCallback,
                xRange: this.viewWidth,
                yRange: GRAPH_HEIGHT,
                tooltip: i18n['search.numericParametricFields.tooltip']
            });

            this.selectedParametricValues.where({
                field: this.fieldName
            }).forEach(function (restriction) {
                const range = restriction.get('range');
                if (range) {
                    //noinspection JSUnresolvedFunction
                    this.$minInput.val(roundInputNumber(range[0]));
                    //noinspection JSUnresolvedFunction
                    this.$maxInput.val(roundInputNumber(range[1]));

                    graph.selectionRect.init(graph.chart, GRAPH_HEIGHT, graph.scale.barWidth(range[0]));
                    graph.selectionRect.update(graph.scale.barWidth(range[1]));
                    graph.selectionRect.focus();
                }
            }, this);
        }
    });
});