/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([], function () {
    "use strict";
    
    return {
        element: null,
        previousElement: null,
        currentX: 0,
        originX: 0,
        setElement: function (ele) {
            this.previousElement = this.element;
            this.element = ele;
        },
        getNewAttributes: function () {
            let x = this.currentX < this.originX ? this.currentX : this.originX;
            let width = Math.abs(this.currentX - this.originX);
            return {
                x: x,
                width: width
            };
        },
        getCurrentAttributes: function () {
            let x = +this.element.attr("x");
            let width = +this.element.attr("width");
            return {
                x1: x,
                x2: x + width
            };
        },
        init: function (chart, height, newX) {
            let rectElement = chart.append("rect")
                .attr({
                    rx: 4,
                    ry: 4,
                    x: newX,
                    y: 0,
                    width: 0,
                    height: height
                })
                .classed("selection", true);
            this.setElement(rectElement);
            this.originX = newX;
            this.update(newX);
        },
        update: function (newX) {
            this.currentX = newX;
            this.element.attr(this.getNewAttributes());
        },
        focus: function () {
            this.element
                .style("stroke", "#01a982")
                .style("stroke-width", "2.5");
        },
        remove: function () {
            this.element.remove();
            this.element = null;
        },
        removePrevious: function () {
            if (this.previousElement) {
                this.previousElement.remove();
            }
        }
    }
});