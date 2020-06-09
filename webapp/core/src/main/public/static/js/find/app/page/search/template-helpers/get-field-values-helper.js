/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
        const delimiter = options.hash.delimiter != null ? options.hash.delimiter : options.fn ? ' ' : ', ';
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
