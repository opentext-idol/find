/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'underscore',
    'parametric-refinement/prettify-field-name',
    'parametric-refinement/selected-values-collection',
    'd3',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filters/parametric/numeric-parametric-field-view.html'
], function (Backbone, $, _, prettifyFieldName, SelectedParametricValuesCollection, d3, i18n, template) {
    "use strict";
    const DEFAULT_TARGET_NUMBER_OF_BUCKETS = 10;
    const GRAPH_HEIGHT = 110;
    // the amount of relative space to add above the highest data point
    const MAX_HEIGHT_MULTIPLIER = 7 / 6;
    const MAX_WIDTH_MODIFIER = 1;
    const BAR_GAP_SIZE = 1;
    const EMPTY_BAR_HEIGHT = 1;
    const UPDATE_DEBOUNCE_WAIT_TIME = 1000;

    function getData(numericFieldValuesWithCount) {
        //noinspection JSUnresolvedFunction
        let minValue = +_.first(numericFieldValuesWithCount).value;
        //noinspection JSUnresolvedFunction
        let maxValue = +_.last(numericFieldValuesWithCount).value;
        let bucketSize = Math.ceil((maxValue - minValue + 1) / DEFAULT_TARGET_NUMBER_OF_BUCKETS);
        let buckets = [];
        let valueIndex = 0;
        numericFieldValuesWithCount.forEach(function (valueAndCount) {
            let relativeValue = minValue >= 0 ? valueAndCount.value - minValue : valueAndCount.value + minValue;
            while (valueIndex < relativeValue) {
                let currentBucketIndex = Math.floor(valueIndex / bucketSize);
                let value = minValue + valueIndex;
                if (buckets[currentBucketIndex]) {
                    buckets[currentBucketIndex].maxValue = value;
                } else {
                    buckets[currentBucketIndex] = {
                        count: 0,
                        minValue: value,
                        maxValue: value
                    };
                }

                valueIndex++;
            }

            let currentBucketIndex = Math.floor(valueIndex / bucketSize);
            let value = minValue + valueIndex;
            if (buckets[currentBucketIndex]) {
                buckets[currentBucketIndex].count += valueAndCount.count;
                buckets[currentBucketIndex].maxValue = value;
            } else {
                buckets[currentBucketIndex] = {
                    count: valueAndCount.count,
                    minValue: value,
                    maxValue: value
                };
            }

            valueIndex++;
        });

        //noinspection JSUnresolvedFunction
        var counts = _.pluck(buckets, 'count');
        return {
            maxValue: Math.max.apply(Math, counts),
            minValue: Math.min.apply(Math, counts),
            bucketSize: bucketSize,
            buckets: buckets
        };
    }

    function drawGraph(data) {
        let scale = {
            barWidth: d3.scale.linear(),
            y: d3.scale.linear()
        };

        //noinspection JSUnresolvedVariable
        let totalWidth = this.viewWidth;

        scale.barWidth.domain([0, data.bucketSize]);
        scale.barWidth.range([0, totalWidth * MAX_WIDTH_MODIFIER / data.buckets.length - BAR_GAP_SIZE]);
        scale.y.domain([0, data.maxValue * MAX_HEIGHT_MULTIPLIER]);
        scale.y.range([GRAPH_HEIGHT, 0]);

        //noinspection JSUnresolvedFunction
        let chart = d3.select(this.$('.chart')[0])
            .attr({
                width: totalWidth,
                height: GRAPH_HEIGHT
            });
        let bars = chart
            .selectAll('g')
            .data(data.buckets)
            .enter()
            .append('g');
        bars.append('rect')
            .attr({
                x: function (d, i) {
                    return i * scale.barWidth(data.bucketSize);
                },
                y: function (d) {
                    return d.count ? scale.y(d.count) : GRAPH_HEIGHT - EMPTY_BAR_HEIGHT;
                },
                height: function (d) {
                    return d.count ? GRAPH_HEIGHT - scale.y(d.count) : EMPTY_BAR_HEIGHT;
                },
                width: function (d) {
                    return scale.barWidth(d.maxValue - d.minValue + 1) - BAR_GAP_SIZE;
                },
                'bucket-min': function (d) {
                    return d.minValue;
                },
                'bucket-max': function (d) {
                    return d.maxValue;
                }
            });
    }

    function resetSelectedParametricValues(selectedParametricValues, fieldName) {
        let existingRestrictions = selectedParametricValues.where({field: fieldName});
        existingRestrictions.forEach(function (model) {
            selectedParametricValues.remove(model);
        });
    }

    function updateMin($minInput, result) {
        //noinspection JSUnresolvedFunction
        let minValue = _.first(result.values).value;
        if (minValue !== $minInput.val()) {
            $minInput.val(minValue);
            $minInput.trigger('change');
        }
    }

    function updateMax($maxInput, result) {
        //noinspection JSUnresolvedFunction
        let maxValue = _.last(result.values).value;
        if (maxValue !== $maxInput.val()) {
            $maxInput.val(maxValue);
            $maxInput.trigger('change');
        }
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
                    updateMin($minInput, result);
                });
            },
            'click .numeric-parametric-no-max': function () {
                //noinspection JSUnresolvedVariable
                let $maxInput = this.$maxInput;
                //noinspection JSUnresolvedFunction
                this.executeCallbackWithoutRestrictions(function (result) {
                    updateMax($maxInput, result);
                });
            },
            'click .numeric-parametric-reset': function () {
                //noinspection JSUnresolvedVariable
                let $minInput = this.$minInput;
                //noinspection JSUnresolvedVariable
                let $maxInput = this.$maxInput;
                //noinspection JSUnresolvedFunction
                this.executeCallbackWithoutRestrictions(function (result) {
                    updateMin($minInput, result);
                    updateMax($maxInput, result);
                });
            },
            'click [bucket-min]': function (e) {
                let $target = $(e.currentTarget);
                //noinspection JSUnresolvedFunction,JSUnresolvedVariable
                this.updateRestrictions(this.selectedParametricValues, this.fieldName, $target.attr('bucket-min'), $target.attr('bucket-max'));
            },
            'slide .numeric-parametric-slider': function(e) {
                //noinspection JSUnresolvedVariable
                this.$minInput.val(Number(e.value[0]));
                //noinspection JSUnresolvedVariable
                this.$maxInput.val(Number(e.value[1]));
            },
            'slideStop .numeric-parametric-slider': function() {
                //noinspection JSUnresolvedVariable
                this.$minInput.trigger('change');
                //noinspection JSUnresolvedVariable
                this.$maxInput.trigger('change');
            },
            'change .numeric-parametric-min-input': function () {
                //noinspection JSUnresolvedFunction,JSUnresolvedVariable
                this.updateRestrictionsAfterDelay(this.selectedParametricValues, this.fieldName, this.$minInput.val(), this.$maxInput.val());
            },
            'change .numeric-parametric-max-input': function () {
                //noinspection JSUnresolvedFunction,JSUnresolvedVariable
                this.updateRestrictionsAfterDelay(this.selectedParametricValues, this.fieldName, this.$minInput.val(), this.$maxInput.val());
            }
        },

        initialize: function (options) {
            this.queryModel = options.queryModel;
            this.selectedParametricValues = options.selectedParametricValues;
            this.viewWidth = options.viewWidth;
            this.fieldName = this.model.id;
        },

        render: function () {
            //noinspection JSUnresolvedVariable,JSUnresolvedFunction
            this.$el.empty().append(this.template({
                i18n: i18n,
                fieldName: prettifyFieldName(this.model.get('name')),
                id: _.uniqueId('numeric-parametric-field')
            }));

            let numericFieldValuesWithCount = this.model.get('values');
            var data = getData(numericFieldValuesWithCount);

            drawGraph.call(this, data);

            //noinspection JSUnresolvedFunction
            let minValue = +_.first(data.buckets).minValue;
            //noinspection JSUnresolvedFunction
            let maxValue = +_.last(data.buckets).maxValue;

            //noinspection JSUnresolvedFunction
            this.executeCallbackWithoutRestrictions(_.bind(function (result) {
                //noinspection JSUnresolvedFunction
                this.$('.numeric-parametric-slider')
                    .slider({
                        min: +_.first(result.values).value,
                        max: +_.last(result.values).value,
                        tooltip: 'hide',
                        value: [minValue, maxValue]
                    });
            }, this));

            //noinspection JSUnresolvedFunction
            this.$minInput = this.$('.numeric-parametric-min-input');
            //noinspection JSUnresolvedFunction
            this.$maxInput = this.$('.numeric-parametric-max-input');

            //noinspection JSUnresolvedFunction
            this.$minInput.val(minValue);
            //noinspection JSUnresolvedFunction
            this.$maxInput.val(maxValue);
        },
        
        updateRestrictions: function(selectedParametricValues, fieldName, min, max) {
            resetSelectedParametricValues(selectedParametricValues, fieldName);
            selectedParametricValues.add({
                field: fieldName,
                range: [min, max]
            });
        },
        
        updateRestrictionsAfterDelay: function (selectedParametricValues, fieldName, min, max) {
            //noinspection JSUnresolvedFunction
            _.debounce(this.updateRestrictions, UPDATE_DEBOUNCE_WAIT_TIME)(selectedParametricValues, fieldName, min, max);
        },

        executeCallbackWithoutRestrictions: function (callback) {
            let fieldName = this.fieldName;
            let otherRestrictions = this.selectedParametricValues.filter(function (model) {
                return model.get('field') !== fieldName;
            });
            let clonedCollection = new SelectedParametricValuesCollection(otherRestrictions);
            let self = this;
            $.ajax({
                    url: '../api/public/parametric/numeric',
                    traditional: true,
                    data: {
                        fieldNames: [fieldName],
                        databases: self.queryModel.get('indexes'),
                        queryText: self.queryModel.get('queryText'),
                        fieldText: clonedCollection.toFieldTextNode(),
                        minDate: self.queryModel.getIsoDate('minDate'),
                        maxDate: self.queryModel.getIsoDate('maxDate'),
                        stateTokens: self.queryModel.get('stateMatchIds')
                    }
                })
                .success(function (result) {
                    callback.apply(this, result);
                });
        }
    });
});