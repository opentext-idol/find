/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore'
], function(_) {

    return function(value, options) {
        if (isFinite(value)) {
            const delimiter = options.hash.delimiter || ',';

            const precision = options.hash.precision;
            if (precision || precision === 0) {
                value = Number(value).toFixed(precision)
            }

            const delimited = String(value).replace(/(\d)(?=(\d{3})+(\.|$))/g, function(all, first){
                return first + delimiter;
            });

            return value > 0 && options.hash.plusIfPositive ? '+' + delimited : delimited;
        }

        return value;
    };

});
