/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/util/round-functions'
], function(_, roundFunctions) {

    return function(value, options) {
        if (isFinite(value)) {
            const delimiter = options.hash.delimiter || ',';

            const round = options.hash.round;
            if (round !== undefined) {
                value = roundFunctions(round)(Number(value))
            }

            // We have to split to avoid adding commas after the dot.
            const pair = String(value).split('.');
            pair[0] = pair[0].replace(/(\d)(?=(\d{3})+(\.|$))/g, function(all, first){
                return first + delimiter;
            });
            const delimited = pair.join('.');

            return value > 0 && options.hash.plusIfPositive ? '+' + delimited : delimited;
        }

        return value;
    };

});
