/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * Returns the string with the first letter capitalised.
 */
define(function () {
    return function (s) {
        return s.charAt(0).toUpperCase() + s.substring(1);
    };
});
