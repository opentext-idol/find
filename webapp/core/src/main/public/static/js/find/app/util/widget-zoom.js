/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'd3'
], function(_, d3) {
    'use strict';

    const ZOOM_EXTENT = [0.1, 10];
    const MILLISECONDS_TO_SECONDS = 1000;

    function zoom(scale, scaleType, min, max, zoomCallback) {
        return function() {
            const p = d3.mouse(this);
            const mouseValue = scaleType === 'date'
                ? new Date(scale.invert(p[0])).getTime() / MILLISECONDS_TO_SECONDS
                : scale.invert(p[0]);
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
