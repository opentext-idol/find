define([
    'underscore',
    'find/app/page/search/filters/parametric/numeric-widget-selection-rect',
    'd3'
], function(_, SelectionRect, d3) {
    'use strict';

    const ZOOM_EXTENT = [0.1, 10];

    function zoom(scale, scaleType, min, max, zoomCallback) {
        return function() {
            const p = d3.mouse(this);
            let mouseValue;
            if (scaleType === 'date') {
                 mouseValue = new Date(scale.invert(p[0])).getTime()/1000;
            } else {
                 mouseValue = scale.invert(p[0]);
            }
            if(mouseValue <= max) {
                const zoomScale = d3.event.scale;
                const totalXDiff = (max - min) / zoomScale - (max - min);
                const minValueDiff = totalXDiff * (mouseValue - min) / (max - min);
                const maxValueDiff = totalXDiff * (max - mouseValue) / (max - min);

                zoomCallback(min - minValueDiff, max + maxValueDiff);
            }
        };
    }

    function addZoomBehaviour(options) {
        const zoomBehaviour = d3.behavior.zoom()
            .on('zoom', zoom(options.xScale, options.scaleType, options.minValue, options.maxValue, options.callback))
            .scaleExtent(options.zoomExtent || ZOOM_EXTENT);

        options.chart
            .call(zoomBehaviour)
            .on('mousedown.zoom', null)
            .on('touchstart.zoom', null)
            .on('touchmove.zoom', null)
            .on('touchend.zoom', null);
    }

    return {
        addZoomBehaviour: addZoomBehaviour
    }
});