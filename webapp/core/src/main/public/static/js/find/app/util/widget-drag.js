/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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
    'd3'
], function(_, d3) {
    'use strict';

    const MILLIS_TO_SECONDS = 1000;
    let dragStartX;
    let startMin;
    let startMax;

    function dragStart(scale, scaleType, max, min) {
        return function() {
            startMin = min;
            startMax = max;
            d3.event.sourceEvent.stopPropagation();

            dragStartX = scaleType === 'date'
                ? Math.floor(new Date(scale.invert(d3.event.sourceEvent.pageX)).getTime() / MILLIS_TO_SECONDS)
                : scale.invert(d3.mouse(this)[0]);

        }
    }

    function dragMove(scale, scaleType, dragMoveCallback) {
        return function() {
            const mouseValue = scaleType === 'date'
                ? Math.floor(new Date(scale.invert(d3.event.sourceEvent.pageX)).getTime() / MILLIS_TO_SECONDS)
                : scale.invert(d3.mouse(this)[0]);

            const dragXDifference = dragStartX - mouseValue;
            dragMoveCallback(startMin + dragXDifference, startMax + dragXDifference);
        }
    }

    function dragEnd(scale, scaleType, dragEndCallback) {
        return function() {
            const mouseValue = scaleType === 'date'
                ? Math.floor(new Date(scale.invert(d3.event.sourceEvent.pageX)).getTime() / MILLIS_TO_SECONDS)
                : scale.invert(d3.mouse(this)[0]);

            const dragXDifference = dragStartX - mouseValue;
            dragEndCallback(startMin + dragXDifference, startMax + dragXDifference);
        }
    }

    function addDragBehaviour(options) {
        const dragBehaviour = d3.behavior.drag()
            .on('dragstart', dragStart(options.xScale, options.scaleType, options.max, options.min))
            .on('drag', dragMove(options.xScale, options.scaleType, options.dragMoveCallback))
            .on('dragend', dragEnd(options.xScale, options.scaleType, options.dragEndCallback));

        options.chart.call(dragBehaviour);
    }

    return {
        addDragBehaviour: addDragBehaviour
    }
});
