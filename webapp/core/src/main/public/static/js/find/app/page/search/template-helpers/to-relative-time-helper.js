/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'moment'
], function(_, moment) {

    return function(value, options) {
        if (!value) {
            return value;
        }

        const format = options.hash.dateFormat || 'DD-MM-YYYY';
        const hideSuffix = options.hash.hideSuffix;

        const time = moment(value, format);

        if (options.hash.relativeTo != null) {
            return time.from(moment(options.hash.relativeTo, format), hideSuffix);
        }

        return time.fromNow(hideSuffix);
    };

});