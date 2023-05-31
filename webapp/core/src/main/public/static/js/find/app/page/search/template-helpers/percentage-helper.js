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
