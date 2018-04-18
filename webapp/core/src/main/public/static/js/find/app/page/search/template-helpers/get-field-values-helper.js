/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore'
], function(_) {

    return function(fieldId, options) {
        function transform(list){
            return options.fn ? _.map(list, options.fn) : list;
        }

        const field = _.findWhere(this.fields, {id: fieldId});
        if (!field) {
            return options.inverse ? options.inverse(this) : undefined;
        }

        let values = field.values;
        const delimiter = options.hash.delimiter != null ? options.hash.delimiter : options.fn ? '' : ', ';
        const max = +options.hash.max;
        let ellipsis = '';

        if (max && values.length > max) {
            values = values.slice(0, max);
            ellipsis = options.hash.ellipsis != null ? options.hash.ellipsis : ' …';
        }

        if (!values.length) {
            return options.inverse ? options.inverse(this) : undefined;
        }

        return transform(values).join(delimiter) + ellipsis;
    };

});
