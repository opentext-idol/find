/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore'
], function(_) {
    'use strict';

    function SelectionRect() {
        this.element = null;
        this.previousElement = null;
        this.currentX = 0;
        this.originX = 0;
    }

    function getNewAttributes(currentX, originX) {
        return {
            x: currentX < originX ? currentX : originX,
            width: Math.abs(currentX - originX)
        };
    }

    _.extend(SelectionRect.prototype, {
        setElement: function(element) {
            this.previousElement = this.element;
            this.element = element;
        },

        getCurrentAttributes: function() {
            var x = Number(this.element.attr('x'));

            return {
                x1: x,
                x2: x + Number(this.element.attr('width'))
            };
        },

        init: function(chart, height, newX) {
            var rectElement = chart.append('rect')
                .attr({
                    rx: 4,
                    ry: 4,
                    x: newX,
                    y: 0,
                    width: 0,
                    height: height
                })
                .classed('selection', true);

            this.setElement(rectElement);
            this.originX = newX;
            this.update(newX);
            this.removePrevious();
        },

        update: function(newX) {
            this.currentX = newX;
            this.element.attr(getNewAttributes(this.currentX, this.originX));
        },

        focus: function() {
            this.element
                .style('stroke', '#01a982')
                .style('stroke', 'rgba(97, 71, 103, 0.6)');
        },

        remove: function() {
            if (this.element) {
                this.element.remove();
                this.element = null;
            }
        },

        removePrevious: function() {
            if (this.previousElement) {
                this.previousElement.remove();
            }
        }
    });

    return SelectionRect;

});
