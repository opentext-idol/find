/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
], function() {

    // Regex match conditional block
    return function(pattern, value, options) {
        let matches = value == null ? false : new RegExp(pattern, options.hash.flags || '').test(value);

        return matches ? options.fn(this) : options.inverse(this);
    };

});
