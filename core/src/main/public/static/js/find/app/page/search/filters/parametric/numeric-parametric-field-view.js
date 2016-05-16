/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'parametric-refinement/prettify-field-name',
    'd3',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filters/parametric/numeric-parametric-field-view.html'
], function (Backbone, _, prettifyFieldName, d3, i18n, template) {
    "use strict";
    const DEFAULT_TARGET_NUMBER_OF_BUCKETS = 11;
    const GRAPH_HEIGHT = 150;
    // the amount of relative space to add above the highest data point
    const MAX_HEIGHT_MULTIPLIER = 4 / 3;
    const MAX_WIDTH_MODIFIER = 1;
    const BAR_GAP_SIZE = 1;
    const EMPTY_BAR_HEIGHT = 1;

    function getData() {
        let numericFieldValuesWithCount = this.model.get('values');
        //noinspection JSUnresolvedFunction
        let minValue = _.first(numericFieldValuesWithCount).value;
        //noinspection JSUnresolvedFunction
        let maxValue = _.last(numericFieldValuesWithCount).value;
        let bucketSize = Math.ceil((maxValue - minValue) / (DEFAULT_TARGET_NUMBER_OF_BUCKETS - 1));
        let buckets = [];
        let valueIndex = 0;
        numericFieldValuesWithCount.forEach(function (valueAndCount) {
            let relativeValue = minValue >= 0 ? valueAndCount.value - minValue : valueAndCount.value + minValue;
            while (valueIndex < relativeValue) {
                let currentBucketIndex = Math.floor(valueIndex++ / bucketSize);
                if (buckets[currentBucketIndex]) {
                    buckets[currentBucketIndex].size++;
                } else {
                    buckets[currentBucketIndex] = {
                        size: 1,
                        count: 0
                    };
                }
            }

            let currentBucketIndex = Math.floor(valueIndex++ / bucketSize);
            if (buckets[currentBucketIndex]) {
                buckets[currentBucketIndex].size++;
                buckets[currentBucketIndex].count += valueAndCount.count;
            } else {
                buckets[currentBucketIndex] = {
                    size: 1,
                    count: valueAndCount.count
                };
            }
        });

        //noinspection JSUnresolvedFunction
        var counts = _.pluck(buckets, 'count');
        return {
            minValue: Math.min.apply(Math, counts),
            maxValue: Math.max.apply(Math, counts),
            bucketSize: bucketSize,
            buckets: buckets
        };
    }

    return Backbone.View.extend({
        className: 'animated fadeIn',
        template: _.template(template),

        initialize: function (options) {
            this.viewWidth = options.viewWidth;

            //noinspection JSUnresolvedVariable
            this.$el.attr('data-field', this.model.id);
            //noinspection JSUnresolvedVariable
            this.$el.attr('data-field-display-name', this.model.get('displayName'));
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
            scale.y.domain([data.minValue, data.maxValue * MAX_HEIGHT_MULTIPLIER]);
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
                        return scale.barWidth(d.size) - BAR_GAP_SIZE;
                    }
                });
        }
    });
});