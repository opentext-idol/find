/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/colour-manager
 */
define([
    '../../../underscore/underscore'
], function() {

    var hex2Rgb = function(hex) {
        if(hex[0] === '#') {
            hex = hex.substring(1);
        }

        if(hex.length !== 6) {
            throw new Error('Invalid hex string supplied: ' + hex);
        }

        var rString = hex.substring(0,2);
        var gString = hex.substring(2,4);
        var bString = hex.substring(4,6);

        return {
            r: parseInt(rString, 16),
            g: parseInt(gString, 16),
            b: parseInt(bString, 16)
        }
    };

    var rgb2hsb = function(r, g, b) {
         r = r / 255;
         g = g / 255;
         b = b / 255;

        var max = Math.max(r, g, b);
        var min = Math.min(r, g, b);

        var c = (max - min);
        var s;

        if(max !== 0) {
            s = c / max;
        }
        else {
            s = 0;
        }

        var h;

        if(c === 0) {
            h = 0;
        }
        else if(max === r) {
            h = (g - b) / c;
        }
        else if(max === g) {
            h = 2 + (b - r) / c;
        }
        else {
            h = 4 + (r - g) / c;
        }

        // +360 to avoid changes when compared to Raphael
        h = ((h + 360) % 6) / 6;

        return {
            h: h,
            s: s,
            b: max
        }
    };

    var hsb2rgb = function(h,s,v) {
        var c = v * s;
        var hp = h * 6;
        var x = c * (1 - Math.abs(hp % 2 - 1));

        // r,g,b
        var matrix = [
            [c,x,0],
            [x,c,0],
            [0,c,x],
            [0,x,c],
            [x,0,c],
            [c,0,x]
        ];

        var row = matrix[Math.floor(hp)];
        var m = v - c;
        var r = Math.floor(255 * (row[0] + m));
        var g = Math.floor(255 * (row[1] + m));
        var b = Math.floor(255 * (row[2] + m));

        var out = '#';

        if(r < 16) {
            out += '0';
        }
        out += r.toString(16);

        if(g < 16) {
            out += '0';
        }
        out += g.toString(16);

        if(b < 16) {
            out += '0';
        }
        out += b.toString(16);

        return out;
    };

    // getColour maps user-defined keys to a (hopefully) unique colour.

    var predefinedColours = function() {
        return ['#edc240', '#afd8f8', '#cb4b4b', '#1f77b4', '#ff7f0e', '#2ca02c', '#8c564b',
            '#e377c2', '#7f7f7f', '#bcbd22', '#17becf'];
    };

    var generateColour =  function() {
        return '#' + Math.floor(Math.random() * (0xFFFFFF - 0x100000) + 0x100000).toString(16);
    };

    /**
     * @name module:js-whatever/js/colour-manager.ColourManager
     * @desc Associates keys with colours to allow colours to be applied consistently to keys without clashes
     * @constructor
     */
    var ColourManager = function() {
        this.colourList = [];
        this.colourMap = {};

        this.reset();
    };

    _.extend(ColourManager.prototype, /** @lends module:js-whatever/js/colour-manager.ColourManager.prototype */{
        /**
         * @desc Reset the ColourManager, restoring the list of predefined colours
         */
        reset: function() {
            this.colourList = predefinedColours();
            this.colourMap = {};
        },

        /**
         * @desc Get the colour for a given key.  This will assign a new colour if one doesn't already exist
         * @param {string} key The key to associate with a colour
         * @returns {string} The hex value of the colour
         */
        getColour: function(key) {
            if (this.colourMap[key]) {
                return this.colourMap[key];
            }

            var newColour = _.find(this.colourList, function(colour) {
                return !_.contains(this.colourMap, colour);
            }, this);

            if (!newColour) {
                newColour = generateColour();
                this.colourList.push(newColour);
            }

            this.colourMap[key] = newColour;

            return newColour;
        },

        /**
         * @desc Removes the colour for the given key, freeing it up for reuse
         * @param {string} key The key to remove
         */
        deleteColour: function(key) {
            delete this.colourMap[key];
        },

        /**
         * Multiplies the saturation of a given hex value by fraction, capping the resulting value at 1
         * @param {string} hexIn The initial hex value
         * @param {number} fraction The multiplier for the saturation. This should be > 0
         * @returns {string} The new hex value
         */
        changeSaturation: function (hexIn, fraction) {
            var rgb = hex2Rgb(hexIn);
            var hsb = rgb2hsb(rgb.r, rgb.g, rgb.b);
            hsb.s = Math.min(1, hsb.s * fraction);
            return hsb2rgb(hsb.h, hsb.s, hsb.b);
        }
    });

    return ColourManager;

});