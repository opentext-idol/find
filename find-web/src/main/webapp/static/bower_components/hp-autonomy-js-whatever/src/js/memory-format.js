/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/memory-format
 */
define(function(){
    //noinspection UnnecessaryLocalVariableJS
    /**
     * @alias module:js-whatever/js/memory-format
     * @desc Formats a number of bytes to one decimal place in the largest available unit
     * @param {number} bytes The number of bytes to format
     * @param {number} [multiple] The number of bytes will be multiplied by this before being formatted. Useful if
     * the value you need to format is not in bytes
     * @returns {string} A formatted string
     * @example
     * memoryFormat(10); // returns '10 B'
     * memoryFormat(1024); // returns '1.0 KB'
     * memoryFormat(2, 1024); // returns '2.0 KB'
     */
    var memoryFormat = function(bytes, multiple) {
        if (multiple) {
            bytes *= multiple;
        }

        if (!isFinite(bytes)) {
            return String(bytes);
        }

        var magnitude = Math.abs(bytes);

        if (magnitude >= 1099511627776) {
            return (bytes / 1099511627776).toFixed(1) + ' TB';
        }
        else if (magnitude >= 1073741824) {
            return (bytes / 1073741824).toFixed(1) + ' GB';
        }
        else if (magnitude >= 1048576) {
            return (bytes / 1048576).toFixed(1) + ' MB';
        }
        else if (magnitude >= 1024) {
            return (bytes / 1024).toFixed(1) + ' KB';
        }

        return Number(bytes).toFixed(0) + ' B';
    };

    return memoryFormat;
});