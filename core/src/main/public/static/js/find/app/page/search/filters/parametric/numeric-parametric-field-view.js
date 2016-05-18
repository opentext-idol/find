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
    const GRAPH_HEIGHT = 125;
    // the amount of relative space to add above the highest data point
    const MAX_HEIGHT_MULTIPLIER = 4 / 3;
    const MAX_WIDTH_MODIFIER = 1;
    const BAR_GAP_SIZE = 1;
    const EMPTY_BAR_HEIGHT = 1;

    function getData() {
        let numericFieldValuesWithCount = this.model.get('values');
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

    function resetSelectedParametricValues(selectedParametricValues, fieldName) {
        let existingRestrictions = selectedParametricValues.where({field: fieldName});
        existingRestrictions.forEach(function (model) {
            selectedParametricValues.remove(model);
        });
    }

    return Backbone.View.extend({
        className: 'animated fadeIn',
        template: _.template(template),

        events: {
            'click .numeric-parametric-no-min': function () {
                //noinspection JSUnresolvedFunction
                let $minInput = this.$('.numeric-parametric-min-input');
                //noinspection JSUnresolvedFunction
                this.executeCallbackWithoutRestrictions(function (result) {
                    //noinspection JSUnresolvedFunction
                    $minInput.val(_.first(result.values).value);
                    $minInput.trigger('change');
                });
            },
            'click .numeric-parametric-no-max': function () {
                //noinspection JSUnresolvedFunction
                let $maxInput = this.$('.numeric-parametric-max-input');
                //noinspection JSUnresolvedFunction
                this.executeCallbackWithoutRestrictions(function (result) {
                    //noinspection JSUnresolvedFunction
                    $maxInput.val(_.last(result.values).value);
                    $maxInput.trigger('change');
                });
            },
            'click .numeric-parametric-reset': function () {
                //noinspection JSUnresolvedFunction
                let $minInput = this.$('.numeric-parametric-min-input');
                //noinspection JSUnresolvedFunction
                let $maxInput = this.$('.numeric-parametric-max-input');
                //noinspection JSUnresolvedFunction
                this.executeCallbackWithoutRestrictions(function (result) {
                    //noinspection JSUnresolvedFunction
                    $minInput.val(_.first(result.values).value);
                    $minInput.trigger('change');
                    //noinspection JSUnresolvedFunction
                    $maxInput.val(_.last(result.values).value);
                    $maxInput.trigger('change');
                });
            },
            'click [bucket-min]': function (e) {
                //noinspection JSUnresolvedVariable
                let selectedParametricValues = this.selectedParametricValues;
                //noinspection JSUnresolvedVariable
                let fieldName = this.fieldName;

                let $target = $(e.currentTarget);

                resetSelectedParametricValues(selectedParametricValues, fieldName);

                selectedParametricValues.add({
                    field: fieldName,
                    range: [$target.attr('bucket-min'), $target.attr('bucket-max')]
                });
            },
            'change .numeric-parametric-min-input': function () {
                console.log('min input change'); //TODO
            },
            'change .numeric-parametric-max-input': function () {
                console.log('max input change'); //TODO
            }
        },

        initialize: function (options) {
            this.queryModel = options.queryModel;
            this.selectedParametricValues = options.selectedParametricValues;
            this.viewWidth = options.viewWidth;
            this.fieldName = this.model.id;
        },

        render: function () {
            //noinspection JSUnresolvedVariable
            this.$el.empty().append(this.template({
                i18n: i18n,
                fieldName: prettifyFieldName(this.model.get('name'))
            }));
            var data = getData.call(this);

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

            //noinspection JSUnresolvedFunction
            this.$('.numeric-parametric-min-input').attr('value', _.first(data.buckets).minValue);
            //noinspection JSUnresolvedFunction
            this.$('.numeric-parametric-max-input').attr('value', _.last(data.buckets).maxValue);
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