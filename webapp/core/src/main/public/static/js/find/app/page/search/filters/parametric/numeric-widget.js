/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'underscore',
    'd3',
    'find/app/page/search/filters/parametric/numeric-widget-selection-rect',
    'find/app/util/widget-zoom'
], function(_, d3, SelectionRect, widgetZoom) {
    'use strict';

    const BAR_GAP_SIZE = 1;
    const EMPTY_BAR_HEIGHT = 1;

    function dragStart(chart, height, selectionRect) {
        return function() {
            d3.event.sourceEvent.stopPropagation();
            const p = d3.mouse(this);
            selectionRect.init(chart, height, p[0]);
        };
    }

    function dragMove(scale, updateCallback, selectionRect) {
        return function() {
            const p = d3.mouse(this);
            selectionRect.update(p[0]);
            const currentAttributes = selectionRect.getCurrentAttributes();
            updateCallback(scale.invert(currentAttributes.x1), scale.invert(currentAttributes.x2));
        };
    }

    function dragEnd(scale, selectionCallback, deselectionCallback, selectionRect) {
        return function() {
            const finalAttributes = selectionRect.getCurrentAttributes();

            if(finalAttributes.x2 - finalAttributes.x1 > 1) {
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

    return function(options) {
        const barGapSize = options.barGapSize || BAR_GAP_SIZE;
        const emptyBarHeight = options.emptyBarHeight || EMPTY_BAR_HEIGHT;
        const formattingFn = options.formattingFn || _.identity;

        return {
            // options.data must be an ordered array of non-overlapping buckets
            drawGraph: function(options) {
                const data = options.data;
                const minValue = data[0].min;
                const maxValue = _.last(data).max;

                const scale = {
                    x: d3.scale.linear()
                        .domain([minValue, maxValue])
                        .range([0, options.xRange]),
                    y: d3.scale.linear()
                        .domain([0, _.max(_.pluck(data, 'count'))])
                        .range([0, options.yRange])
                };

                const chart = d3.select(options.chart)
                    .attr({
                        width: options.xRange,
                        height: options.yRange
                    });

                // move origin to bottom left
                const group = chart.append('g')
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
                        x: function(d) {
                            return scale.x(d.min);
                        },
                        y: function() {
                            return 0;
                        },
                        height: function(d) {
                            // If the computed bar height would be less than
                            // the emptyBarHeight, use the emptyBarHeight instead
                            return Math.max(scale.y(d.count), emptyBarHeight);
                        },
                        width: function(d) {
                            const scaledWidth = scale.x(d.max) - scale.x(d.min);
                            return Math.max(scaledWidth - barGapSize, 0);
                        }
                    })
                    .append('title')
                    .text(function(d) {
                        return options.tooltip(formattingFn(d.min), formattingFn(d.max), d.count);
                    });

                if(options.coordinatesEnabled) {
                    chart.on('mousemove', function() {
                        options.mouseMoveCallback(scale.x.invert(d3.mouse(this)[0]));
                    });

                    chart.on('mouseleave', function() {
                        options.mouseLeaveCallback();
                    });
                }

                const selectionRect = new SelectionRect();

                if(options.dragEnabled) {
                    const dragBehaviour = d3.behavior.drag()
                        .on('drag', dragMove(scale.x, options.updateCallback, selectionRect))
                        .on('dragstart', dragStart(chart, options.yRange, selectionRect))
                        .on('dragend',
                            dragEnd(
                                scale.x,
                                options.selectionCallback,
                                options.deselectionCallback,
                                selectionRect
                            )
                        );

                    chart.call(dragBehaviour);
                }

                if(options.zoomEnabled) {
                    widgetZoom.addZoomBehaviour({
                        chart: chart,
                        xScale: scale.x,
                        min: minValue,
                        max: maxValue,
                        callback: options.zoomCallback
                    });
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
