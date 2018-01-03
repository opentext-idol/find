/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define(function() {
    return function(a, b) {
        let match = a === b;

        const numToCompare = arguments.length - 1;
        const options = arguments[numToCompare]

        if (!match && numToCompare > 2) {
            for (let ii = 2; ii < numToCompare; ++ii) {
                if (a === arguments[ii]) {
                    match = true;
                    break;
                }
            }
        }

        return match ? options.fn(this) : options.inverse(this);
    };
});
