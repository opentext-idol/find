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

    function dragMove(scale, updateCallback, selectionRect) {
        return function () {
            var p = d3.mouse(this);
            selectionRect.update(p[0]);
            var currentAttributes = selectionRect.getCurrentAttributes();
            updateCallback(scale.invert(currentAttributes.x1), scale.invert(currentAttributes.x2));
        };
    }

    function dragEnd(scale, selectionCallback, deselectionCallback, selectionRect) {
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
                deselectionCallback();
            }
        };
    }

    function zoom(barScale, min, max, zoomCallback) {
        return function () {
            var p = d3.mouse(this);
            var mouseValue = barScale.invert(p[0]);

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
            // options.data must be an ordered array of non-overlapping buckets 
            drawGraph: function (options) {
                var data = options.data;
                var minValue = data[0].min;
                var maxValue = _.last(data).max;

                var scale = {
                    x: d3.scale.linear()
                        .domain([minValue, maxValue])
                        .range([0, options.xRange]),
                    y: d3.scale.linear()
                        .domain([0, _.max(_.pluck(data, 'count'))])
                        .range([0, options.yRange])
                };

                //noinspection JSUnresolvedFunction
                var chart = d3.select(options.chart)
                    .attr({
                        width: options.xRange,
                        height: options.yRange
                    });

                // move origin to bottom left
                var group = chart.append('g')
                    .attr({
                        transform: 'translate(0 ' + options.yRange + ') scale(1 -1)'
                    });

                group
                    .selectAll('g')
                    .data(data)
                    .enter()
                        .append('g')
                            .append('rect')
                            .attr({
                                x: function (d) {
                                    return scale.x(d.min);
                                },
                                y: function () {
                                    return 0;
                                },
                                height: function (d) {
                                    // If the computed bar height would be less than the emptyBarHeight, use the emptyBarHeight instead
                                    return Math.max(scale.y(d.count), emptyBarHeight);
                                },
                                width: function (d) {
                                    var scaledWidth = scale.x(d.max) - scale.x(d.min);
                                    return Math.max(scaledWidth - barGapSize, 0);
                                }
                            })
                            .append('title')
                                .text(function (d) {
                                    return options.tooltip(formattingFn(d.min), formattingFn(d.max), d.count);
                                });

                if (options.coordinatesEnabled) {
                    chart.on('mousemove', function () {
                        options.mouseMoveCallback(scale.x.invert(d3.mouse(this)[0]));
                    });
                    
                    chart.on('mouseleave', function () {
                        options.mouseLeaveCallback();
                    });
                }

                var selectionRect = new SelectionRect();

                if (options.dragEnabled) {
                    var dragBehaviour = d3.behavior.drag()
                        .on('drag', dragMove(scale.x, options.updateCallback, selectionRect))
                        .on('dragstart', dragStart(chart, options.yRange, selectionRect))
                        .on('dragend', dragEnd(scale.x, options.selectionCallback, options.deselectionCallback, selectionRect));

                    chart.call(dragBehaviour);
                }

                if (options.zoomEnabled) {
                    var zoomBehaviour = d3.behavior.zoom()
                        .on('zoom', zoom(scale.x, minValue, maxValue, options.zoomCallback))
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
                        selectionRect.init(chart, options.yRange, scale.x(range[0]));
                        selectionRect.update(scale.x(range[1]));
                        selectionRect.focus();
                    }
                };
            }
        };
    };
});