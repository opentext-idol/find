/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore'
], function(_) {

    return function(fieldId, value, options) {
        const hasField = !!_.find(this.fields, function(field) {
            return field.id === fieldId && _.contains(field.values, value);
        });

        return hasField ? options.fn(this) : options.inverse(this);
    };

});

