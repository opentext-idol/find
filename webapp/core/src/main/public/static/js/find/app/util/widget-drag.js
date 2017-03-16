define([
    'underscore',
    'find/app/page/search/filters/parametric/numeric-widget-selection-rect',
    'd3'
], function(_, SelectionRect, d3) {
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
            if (scaleType === 'date') {
                dragStartX = Math.floor(new Date(scale.invert(d3.event.sourceEvent.x)).getTime()/MILLIS_TO_SECONDS);
            } else {
                dragStartX = scale.invert(d3.mouse(this)[0]);
            }
        }
    }

    function dragMove(scale, scaleType, dragMoveCallback) {
        return function() {
            let mouseValue;
            if (scaleType === 'date') {
                mouseValue = Math.floor(new Date(scale.invert(d3.event.sourceEvent.x)).getTime()/MILLIS_TO_SECONDS);
            } else {
                mouseValue = scale.invert(d3.mouse(this)[0]);
            }
            const dragXDifference =  dragStartX - mouseValue;
            dragMoveCallback(startMin + dragXDifference, startMax + dragXDifference);
        }
    }

    function dragEnd(scale, scaleType, dragEndCallback) {
        return function() {
            let mouseValue;
            if (scaleType === 'date') {
                mouseValue = Math.floor(new Date(scale.invert(d3.event.sourceEvent.x)).getTime()/MILLIS_TO_SECONDS);
            } else {
                mouseValue = scale.invert(d3.mouse(this)[0]);
            }
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