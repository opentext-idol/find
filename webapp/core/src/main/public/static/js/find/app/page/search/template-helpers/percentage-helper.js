/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/util/round-functions'
], function(_, roundFunctions) {

    return function(value, options) {
        if (!isFinite(value) || value === null || value === '') {
            return value;
        }
        const max = options.hash.max || 100;
        const min = options.hash.min || 0;
        const percentage = ((value - min) / (max - min)) * 100;

        const round = options.hash.round;
        if (round !== undefined) {
            return roundFunctions(round)(Number(value)) + '%';
        }

        return percentage + '%';
    };

});
