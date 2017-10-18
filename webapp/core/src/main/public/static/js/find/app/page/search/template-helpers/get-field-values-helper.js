/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore'
], function(_) {

    return function(fieldId, options) {
        const field = _.findWhere(this.fields, {id: fieldId});
        if (!field) {
            return undefined;
        }

        let values = field.values;
        const delimiter = options.hash.delimiter != null ? options.hash.delimiter : ', ';
        const max = +options.hash.max;

        if (max && values.length > max) {
            values = values.slice(0, max);

            const ellipsis = options.hash.ellipsis != null ? options.hash.ellipsis : ' …';
            if (ellipsis) {
                return values.join(delimiter) + ellipsis;
            }
        }

        return values.join(delimiter);
    };

});
