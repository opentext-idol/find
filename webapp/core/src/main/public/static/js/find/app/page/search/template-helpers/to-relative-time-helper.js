/*
 * Copyright 2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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

        const format = options.hash.dateFormat || 'L';
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
