/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'moment',
    'find/app/util/round-functions'
], function(_, moment, roundFunctions) {

    return function(value, options) {
        if (!value) {
            return value;
        }

        const format = options.hash.dateFormat || 'DD-MM-YYYY';
        const hideSuffix = options.hash.hideSuffix;

        const roundFn = roundFunctions(options.hash.round);

        const time = moment(value, format);

        const defaultRounding = moment.relativeTimeRounding();

        try {
            // Set the rounding
            moment.relativeTimeRounding(roundFn);

            if (options.hash.relativeTo != null) {
                return time.from(moment(options.hash.relativeTo, format), hideSuffix);
            }

            return time.fromNow(hideSuffix);
        } finally {
            // Reset the rounding
            moment.relativeTimeRounding(defaultRounding);
        }
    };

});