/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * Return a compact JSON representation of a value.
 */
define(function () {
    return function (obj) {
        return JSON.stringify(obj);
    };
});
