/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/page/search/filters/parametric/numeric-widget-selection-rect',
    'd3'
], function (_, SelectionRect, d3) {
    'use strict';

    var BAR_GAP_SIZE = 1;
    var EMPTY_BAR_HEIGHT = 1;
    var ZOOM_EXTENT = [0.1, 10];

    function dragStart(chart, height, selectionRect) {
        return function () {
            d3.event.sourceEvent.stopPropagation();
            var p = d3.mouse(this);
            selectionRect.init(chart, height, p[0]);
        };
    }

    function dragMove(scale, min, updateCallback, selectionRect) {
        return function () {
            var p = d3.mouse(this);
            selectionRect.update(p[0]);
            var currentAttributes = selectionRect.getCurrentAttributes();
            updateCallback(min + scale.invert(currentAttributes.x1), min + scale.invert(currentAttributes.x2));
        };
    }

    function dragEnd(scale, min, selectionCallback, deselectionCallback, selectionRect) {
        return function () {
            var finalAttributes = selectionRect.getCurrentAttributes();

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
        };
    }

    function zoom(barScale, min, max, zoomCallback) {
        return function () {
            var p = d3.mouse(this);
            var mouseValue = min + barScale.invert(p[0]);
            if (mouseValue <= max) {
                var zoomScale = d3.event.scale;
                var totalXDiff = (max - min) / zoomScale - (max - min);
                var minValueDiff = totalXDiff * (mouseValue - min) / (max - min);
                var maxValueDiff = totalXDiff * (max - mouseValue) / (max - min);

                zoomCallback(min - minValueDiff, max + maxValueDiff);
            }
        };
    }

    return function (options) {
        //noinspection JSUnresolvedVariable
        var barGapSize = options.barGapSize || BAR_GAP_SIZE;
        //noinspection JSUnresolvedVariable
        var emptyBarHeight = options.emptyBarHeight || EMPTY_BAR_HEIGHT;
        //noinspection JSUnresolvedVariable
        var zoomExtent = options.zoomExtent || ZOOM_EXTENT;
        //noinspection JSUnresolvedVariable
        var formattingFn = options.formattingFn || _.identity;

        return {
            drawGraph: function (options) {
                var scale = {
                    barWidth: d3.scale.linear(),
                    y: d3.scale.linear()
                };

                var data = options.data;
                scale.barWidth.domain([0, data.bucketSize]);
                scale.barWidth.range([0, options.xRange / data.buckets.length]);
                scale.y.domain([0, data.maxCount]);
                scale.y.range([options.yRange, 0]);

                //noinspection JSUnresolvedFunction
                var chart = d3.select(options.chart)
                    .attr({
                        width: options.xRange,
                        height: options.yRange
                    });

                var bars = chart
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
                            // If the computed bar height would be less than the emptyBarHeight, use the emptyBarHeight instead
                            var scaledOffset = scale.y(d.count);
                            return options.yRange - scaledOffset > emptyBarHeight ? scaledOffset : options.yRange - emptyBarHeight;
                        },
                        height: function (d) {
                            // If the computed bar height would be less than the emptyBarHeight, use the emptyBarHeight instead
                            var scaledHeight = options.yRange - scale.y(d.count);
                            return scaledHeight > emptyBarHeight ? scaledHeight : emptyBarHeight;
                        },
                        width: function (d) {
                            return Math.max(scale.barWidth(d.max - d.min), barGapSize) - barGapSize;
                        }
                    })
                    .append('title')
                    .text(function (d) {
                        return options.tooltip(formattingFn(d.min), formattingFn(d.max), d.count);
                    });

                if (options.coordinatesEnabled) {
                    chart.on('mousemove', function () {
                        options.mouseMoveCallback(data.minValue + scale.barWidth.invert(d3.mouse(this)[0]));
                    });
                    
                    chart.on('mouseleave', function () {
                        options.mouseLeaveCallback();
                    });
                }

                var selectionRect = new SelectionRect();

                if (options.dragEnabled) {
                    var dragBehaviour = d3.behavior.drag()
                        .on('drag', dragMove(scale.barWidth, data.minValue, options.updateCallback, selectionRect))
                        .on('dragstart', dragStart(chart, options.yRange, selectionRect))
                        .on('dragend', dragEnd(scale.barWidth, data.minValue, options.selectionCallback, options.deselectionCallback, selectionRect));

                    chart.call(dragBehaviour);
                }

                if (options.zoomEnabled) {
                    var zoomBehaviour = d3.behavior.zoom()
                        .on('zoom', zoom(scale.barWidth, data.minValue, data.maxValue, options.zoomCallback))
                        .scaleExtent(zoomExtent);

                    chart
                        .call(zoomBehaviour)
                        .on('mousedown.zoom', null)
                        .on('touchstart.zoom', null)
                        .on('touchmove.zoom', null)
                        .on('touchend.zoom', null);
                }

                return {
                    chart: chart,
                    scale: scale,
                    clearSelection: function() {
                        selectionRect.remove();
                    },
                    setSelection: function(range) {
                        selectionRect.remove();
                        selectionRect.init(chart, options.yRange, scale.barWidth(range[0] - data.minValue));
                        selectionRect.update(scale.barWidth(range[1] - data.minValue));
                        selectionRect.focus();
                    }
                };
            }
        };
    };
});