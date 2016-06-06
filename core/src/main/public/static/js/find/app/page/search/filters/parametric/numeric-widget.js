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
    const ZOOM_EXTENT = [0.1, 10];

    function dragStart(chart, height) {
        return function () {
            d3.event.sourceEvent.stopPropagation();
            const p = d3.mouse(this);
            selectionRect.init(chart, height, p[0]);
        }
    }

    function dragMove(scale, min, updateCallback) {
        return function () {
            const p = d3.mouse(this);
            selectionRect.update(p[0]);
            const currentAttributes = selectionRect.getCurrentAttributes();
            updateCallback(min + scale.invert(currentAttributes.x1), min + scale.invert(currentAttributes.x2));
        };
    }

    function dragEnd(scale, min, selectionCallback, deselectionCallback) {
        return function () {
            const finalAttributes = selectionRect.getCurrentAttributes();
            if (finalAttributes.x2 - finalAttributes.x1 > 1) {
                // range selected
                d3.event.sourceEvent.preventDefault();
                selectionRect.focus();
                selectionCallback(min + scale.invert(finalAttributes.x1), min + scale.invert(finalAttributes.x2));
            } else {
                // single point selected
                selectionRect.remove();
                deselectionCallback();
            }
        }
    }

    function zoom(barScale, min, max, zoomCallback) {
        return function () {
            const p = d3.mouse(this);
            const mouseValue = min + barScale.invert(p[0]);
            if (mouseValue <= max) {
                const zoomScale = d3.event.scale;
                const totalXDiff = (max - min) / zoomScale - (max - min);
                const minValueDiff = totalXDiff * (mouseValue - min) / (max - min);
                const maxValueDiff = totalXDiff * (max - mouseValue) / (max - min);
                
                zoomCallback(min - minValueDiff, max + maxValueDiff);
            }
        }
    }

    return function (options) {
        const barGapSize = options.barGapSize || BAR_GAP_SIZE;
        const emptyBarHeight = options.emptyBarHeight || EMPTY_BAR_HEIGHT;
        const zoomExtent = options.zoomExtent || ZOOM_EXTENT;

        return {
            drawGraph: function (options) {
                const scale = {
                    barWidth: d3.scale.linear(),
                    y: d3.scale.linear()
                };

                const data = options.data;
                scale.barWidth.domain([0, data.bucketSize]);
                scale.barWidth.range([0, options.xRange / data.buckets.length - barGapSize]);
                scale.y.domain([0, data.maxCount]);
                scale.y.range([options.yRange, 0]);

                //noinspection JSUnresolvedFunction
                const chart = d3.select(options.chart)
                    .attr({
                        width: options.xRange,
                        height: options.yRange
                    });
                const bars = chart
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
                            return d.count ? scale.y(d.count) : options.yRange - emptyBarHeight;
                        },
                        height: function (d) {
                            return d.count ? options.yRange - scale.y(d.count) : emptyBarHeight;
                        },
                        width: function (d) {
                            return scale.barWidth(d.max - d.min) - barGapSize;
                        }
                    })
                    .append("title")
                    .text(function (d) {
                        return options.tooltip(d.min, d.max, d.count);
                    });

                const dragBehaviour = d3.behavior.drag()
                    .on("drag", dragMove(scale.barWidth, data.minValue, options.updateCallback))
                    .on("dragstart", dragStart(chart, options.yRange))
                    .on("dragend", dragEnd(scale.barWidth, data.minValue, options.selectionCallback, options.deselectionCallback));
                chart.call(dragBehaviour);

                const zoomBehaviour = d3.behavior.zoom()
                    .on("zoom", zoom(scale.barWidth, data.minValue, data.maxValue, options.zoomCallback))
                    .scaleExtent(zoomExtent);
                chart
                    .call(zoomBehaviour)
                    .on("mousedown.zoom", null)
                    .on("touchstart.zoom", null)
                    .on("touchmove.zoom", null)
                    .on("touchend.zoom", null);

                return {
                    chart: chart,
                    scale: scale,
                    selectionRect: selectionRect
                };
            }
        }
    }
});