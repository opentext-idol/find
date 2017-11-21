/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore'
], function(_) {

    return function(value, options) {
        if (!isFinite(value) || value === null || value === '') {
            return value;
        }
        const max = options.hash.max || 100;
        const min = options.hash.min || 0;
        const percentage = ((value - min) / (max - min)) * 100;

        const precision = options.hash.precision;
        if (precision || precision === 0) {
            return Number(percentage).toFixed(precision) + '%';
        }

        return percentage + '%';
    };

});
