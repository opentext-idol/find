/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define(function() {

    var REGEX = /\S/;

    /**
     * Returns true if the string only contains whitespace characters.
     * @param input The string to test
     * @return {boolean} True if the string is blank
     */
    function stringBlank(input) {
        return !REGEX.test(input);
    }

    return stringBlank;

});
