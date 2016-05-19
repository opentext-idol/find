/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/page/search/filters/parametric/numeric-widget-selection-rect',
    'd3'
], function (_, selectionRect, d3) {
    "use strict";

    const BAR_GAP_SIZE = 1;
    const EMPTY_BAR_HEIGHT = 1;

    function dragStart(chart, height) {
        return function () {
            var p = d3.mouse(this);
            selectionRect.init(chart, height, p[0]);
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

    return function (options) {
        let graphHeight = options.graphHeight;
        let barGapSize = options.barGapSize || BAR_GAP_SIZE;
        let emptyBarHeight = options.emptyBarHeight || EMPTY_BAR_HEIGHT;
        
        return {
            drawGraph: function (options) {
                let scale = {
                    barWidth: d3.scale.linear(),
                    y: d3.scale.linear()
                };

                let data = options.data;
                scale.barWidth.domain([0, data.bucketSize]);
                scale.barWidth.range([0, options.xRange / data.buckets.length - barGapSize]);
                scale.y.domain([0, data.maxCount]);
                scale.y.range([options.yRange, 0]);

                //noinspection JSUnresolvedFunction
                let chart = d3.select(options.chart)
                    .attr({
                        width: options.xRange,
                        height: options.yRange
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
                            return d.count ? scale.y(d.count) : graphHeight - emptyBarHeight;
                        },
                        height: function (d) {
                            return d.count ? graphHeight - scale.y(d.count) : emptyBarHeight;
                        },
                        width: function (d) {
                            return scale.barWidth(d.maxValue - d.minValue + 1) - barGapSize;
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
                        return "Range: " + d.minValue + "-" + (d.maxValue + 1) + "\nCount: " + d.count;
                    });

                let dragBehavior = d3.behavior.drag()
                    .on("drag", dragMove(scale.barWidth, options.updateCallback))
                    .on("dragstart", dragStart(chart, options.yRange))
                    .on("dragend", dragEnd(scale.barWidth, options.selectionCallback));
                chart.call(dragBehavior);

                return {
                    chart: chart,
                    scale: scale,
                    selectionRect: selectionRect
                };
            }
        }
    }
});