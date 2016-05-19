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
    const DEFAULT_TARGET_NUMBER_OF_BUCKETS = 30;
    const GRAPH_HEIGHT = 110;
    // the amount of relative space to add above the highest data point
    const MAX_HEIGHT_MULTIPLIER = 7 / 6;
    const MAX_WIDTH_MODIFIER = 1;
    const BAR_GAP_SIZE = 1;
    const EMPTY_BAR_HEIGHT = 1;
    const UPDATE_DEBOUNCE_WAIT_TIME = 1000;

    var selectionRect = {
        element: null,
        previousElement: null,
        currentX: 0,
        originX: 0,
        setElement: function (ele) {
            this.previousElement = this.element;
            this.element = ele;
        },
        getNewAttributes: function () {
            let x = this.currentX < this.originX ? this.currentX : this.originX;
            let width = Math.abs(this.currentX - this.originX);
            return {
                x: x,
                width: width
            };
        },
        getCurrentAttributes: function () {
            let x = +this.element.attr("x");
            let width = +this.element.attr("width");
            return {
                x1: x,
                x2: x + width
            };
        },
        init: function (chart, newX) {
            let rectElement = chart.append("rect")
                .attr({
                    rx: 4,
                    ry: 4,
                    x: newX,
                    y: 0,
                    width: 0,
                    height: GRAPH_HEIGHT
                })
                .classed("selection", true);
            this.setElement(rectElement);
            this.originX = newX;
            this.update(newX);
        },
        update: function (newX) {
            this.currentX = newX;
            this.element.attr(this.getNewAttributes());
        },
        focus: function () {
            this.element
                .style("stroke", "#01a982")
                .style("stroke-width", "2.5");
        },
        remove: function () {
            this.element.remove();
            this.element = null;
        },
        removePrevious: function () {
            if (this.previousElement) {
                this.previousElement.remove();
            }
        }
    };

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

    function drawGraph(data, updateCallback, selectionCallback) {
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
            })
            .append("title")
            .text(function (d) {
                return (d.maxValue === d.minValue ? "Value: " + d.minValue : "Range: " + d.minValue + "-" + d.maxValue) + "\nCount: " + d.count;
            });

        let dragBehavior = d3.behavior.drag()
            .on("drag", dragMove(scale.barWidth, updateCallback))
            .on("dragstart", dragStart(chart))
            .on("dragend", dragEnd(scale.barWidth, selectionCallback));
        chart.call(dragBehavior);
    }

    function dragStart(chart) {
        return function () {
            var p = d3.mouse(this);
            selectionRect.init(chart, p[0]);
            selectionRect.removePrevious();
        }
    }

    function dragMove(scale, updateCallback) {
        return function () {
            var p = d3.mouse(this);
            selectionRect.update(p[0]);
            var currentAttributes = selectionRect.getCurrentAttributes();
            updateCallback(scale.invert(currentAttributes.x1), scale.invert(currentAttributes.x2));
        };
    }

    function dragEnd(scale, selectionCallback) {
        return function () {
            var finalAttributes = selectionRect.getCurrentAttributes();
            if (finalAttributes.x2 - finalAttributes.x1 > 1) {
                // range selected
                d3.event.sourceEvent.preventDefault();
                selectionRect.focus();
                selectionCallback(scale.invert(finalAttributes.x1), scale.invert(finalAttributes.x2));
            } else {
                // single point selected
                selectionRect.remove();
                selectionCallback();
            }
        }
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
                resetSelectedParametricValues(this.selectedParametricValues, this.fieldName);
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

            //noinspection JSUnresolvedFunction
            let minValue = +_.first(data.buckets).minValue;
            //noinspection JSUnresolvedFunction
            let maxValue = +_.last(data.buckets).maxValue;

            //noinspection JSUnresolvedFunction
            this.$minInput = this.$('.numeric-parametric-min-input');
            //noinspection JSUnresolvedFunction
            this.$maxInput = this.$('.numeric-parametric-max-input');

            //noinspection JSUnresolvedFunction
            this.$minInput.val(minValue);
            //noinspection JSUnresolvedFunction
            this.$maxInput.val(maxValue);

            //noinspection JSUnresolvedFunction
            let updateCallback = _.bind(function (x1, x2) {
                // rounding to one decimal place
                this.$minInput.val(Math.round( x1 * 10 ) / 10);
                this.$maxInput.val(Math.round( x2 * 10 ) / 10);
            }, this);
            //noinspection JSUnresolvedFunction
            let selectionCallback = _.bind(function (x1, x2) {
                let min = x1 || minValue;
                let max = x2 || maxValue;
                this.updateRestrictionsAfterDelay(this.selectedParametricValues, this.fieldName, min, max);
            }, this);
            drawGraph.call(this, data, updateCallback, selectionCallback);
        },

        updateRestrictions: function (selectedParametricValues, fieldName, min, max) {
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