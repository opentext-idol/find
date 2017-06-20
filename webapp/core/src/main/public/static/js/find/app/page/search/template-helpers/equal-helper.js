/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define(function() {
    return function(a, b, options) {
        return a === b ? options.fn(this) : options.inverse(this);
    };
});
