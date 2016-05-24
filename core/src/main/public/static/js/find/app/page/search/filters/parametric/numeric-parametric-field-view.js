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
    const DEFAULT_TARGET_NUMBER_OF_BUCKETS = 30;
    const GRAPH_HEIGHT = 110;
    const UPDATE_DEBOUNCE_WAIT_TIME = 1000;

    function getData(numericFieldValuesWithCount) {
        //noinspection JSUnresolvedFunction
        let minValue = Math.floor(_.first(numericFieldValuesWithCount).value);
        //noinspection JSUnresolvedFunction
        let maxValue = Math.ceil(_.last(numericFieldValuesWithCount).value);
        let bucketSize = Math.ceil((maxValue - minValue + 1) / DEFAULT_TARGET_NUMBER_OF_BUCKETS);
        let buckets = [];
        let valueIndex = 0;
        numericFieldValuesWithCount.forEach(function (valueAndCount) {
            let relativeValue = Math.floor(valueAndCount.value - minValue);
            while (valueIndex < relativeValue) {
                let currentBucketIndex = Math.floor(valueIndex / bucketSize);
                let value = minValue + valueIndex;
                if (buckets[currentBucketIndex]) {
                    buckets[currentBucketIndex].maxValue = value;
                } else {
                    buckets[currentBucketIndex] = {
                        count: 0,
                        minValue: value,
                        maxValue: value,
                        maxContinuousValue: value
                    };
                }

                valueIndex++;
            }

            let currentBucketIndex = Math.floor(valueIndex / bucketSize);
            let value = minValue + valueIndex;
            if (buckets[currentBucketIndex]) {
                buckets[currentBucketIndex].count += valueAndCount.count;
                buckets[currentBucketIndex].maxValue = value;
                buckets[currentBucketIndex].maxContinuousValue = valueAndCount.value;
            } else {
                buckets[currentBucketIndex] = {
                    count: valueAndCount.count,
                    minValue: value,
                    maxValue: value,
                    maxContinuousValue: valueAndCount.value
                };
            }
        });

        //noinspection JSUnresolvedFunction
        var counts = _.pluck(buckets, 'count');
        return {
            maxCount: Math.max.apply(Math, counts),
            minCount: Math.min.apply(Math, counts),
            bucketSize: bucketSize,
            buckets: buckets
        };
    }

    function resetSelectedParametricValues(selectedParametricValues, fieldName) {
        let existingRestrictions = selectedParametricValues.where({field: fieldName});
        existingRestrictions.forEach(function (model) {
            selectedParametricValues.remove(model);
        });
    }

    function updateRestrictions(selectedParametricValues, fieldName, min, max) {
        resetSelectedParametricValues(selectedParametricValues, fieldName);
        selectedParametricValues.add({
            field: fieldName,
            range: [min, max]
        });
    }

    function updateRestrictionsAfterDelay(selectedParametricValues, fieldName, min, max) {
        //noinspection JSUnresolvedFunction
        _.debounce(function (selectedParametricValues, fieldName, min, max) {
            if (min && max) {
                updateRestrictions(selectedParametricValues, fieldName, min, max)
            } else {
                resetSelectedParametricValues(selectedParametricValues, fieldName);
            }
        }, UPDATE_DEBOUNCE_WAIT_TIME)(selectedParametricValues, fieldName, min, max);
    }

    return Backbone.View.extend({
        className: 'animated fadeIn',
        template: _.template(template),

        events: {
            'click .numeric-parametric-no-min': function () {
                //noinspection JSUnresolvedVariable
                let $minInput = this.$minInput;
                //noinspection JSUnresolvedFunction
                this.executeCallbackWithoutRestrictions(function (result) {
                    //noinspection JSUnresolvedFunction
                    let minValue = Math.floor(_.first(result.values).value);
                    if (minValue !== $minInput.val()) {
                        $minInput.val(minValue);
                        $minInput.trigger('change');
                    }
                });
            },
            'click .numeric-parametric-no-max': function () {
                //noinspection JSUnresolvedVariable
                let $maxInput = this.$maxInput;
                //noinspection JSUnresolvedFunction
                this.executeCallbackWithoutRestrictions(function (result) {
                    //noinspection JSUnresolvedFunction
                    let maxValue = Math.ceil(_.last(result.values).value);
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
                updateRestrictionsAfterDelay(this.selectedParametricValues, this.fieldName, this.$minInput.val(), this.$maxInput.val());
            },
            'change .numeric-parametric-max-input': function () {
                //noinspection JSUnresolvedVariable
                updateRestrictionsAfterDelay(this.selectedParametricValues, this.fieldName, this.$minInput.val(), this.$maxInput.val());
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

            function roundInputNumber(x1) {
                return Math.round(x1 * 10) / 10;
            }

            //noinspection JSUnresolvedFunction
            this.executeCallbackWithoutRestrictions(_.bind(function (result) {
                let data = getData(result.values);

                //noinspection JSUnresolvedFunction
                let minValue = Math.floor(+_.first(result.values).value);
                //noinspection JSUnresolvedFunction
                let maxValue = Math.ceil(+_.last(result.values).value);

                //noinspection JSUnresolvedFunction
                this.$minInput.val(minValue);
                //noinspection JSUnresolvedFunction
                this.$maxInput.val(maxValue);

                //noinspection JSUnresolvedFunction
                let updateCallback = _.bind(function (x1, x2) {
                    // rounding to one decimal place
                    this.$minInput.val(roundInputNumber(x1));
                    this.$maxInput.val(roundInputNumber(x2));
                }, this);
                //noinspection JSUnresolvedFunction
                let selectionCallback = _.bind(function (x1, x2) {
                    updateRestrictionsAfterDelay(this.selectedParametricValues, this.fieldName, x1, x2);
                }, this);
                //noinspection JSUnresolvedFunction
                let graph = this.widget.drawGraph({
                    chart: this.$('.chart')[0],
                    data: data,
                    updateCallback: updateCallback,
                    selectionCallback: selectionCallback,
                    xRange: this.viewWidth,
                    yRange: GRAPH_HEIGHT,
                    tooltip: i18n['search.numericParametricFields.tooltip']
                });

                this.selectedParametricValues.where({
                    field: this.fieldName
                }).forEach(function (restriction) {
                    let range = restriction.get('range');
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
            }, this));
        },

        executeCallbackWithoutRestrictions: function (callback) {
            let fieldName = this.fieldName;
            let otherRestrictions = this.selectedParametricValues.filter(function (model) {
                return model.get('field') !== fieldName;
            });
            let clonedCollection = new SelectedParametricValuesCollection(otherRestrictions);
            $.ajax({
                    url: '../api/public/parametric/numeric',
                    traditional: true,
                    data: {
                        fieldNames: [fieldName],
                        databases: this.queryModel.get('indexes'),
                        queryText: this.queryModel.get('queryText'),
                        fieldText: clonedCollection.toFieldTextNode(),
                        minDate: this.queryModel.getIsoDate('minDate'),
                        maxDate: this.queryModel.getIsoDate('maxDate'),
                        minScore: this.queryModel.get('minScore'),
                        stateTokens: this.queryModel.get('stateMatchIds')
                    }
                })
                .success(function (result) {
                    callback.apply(this, result);
                });
        }
    });
});