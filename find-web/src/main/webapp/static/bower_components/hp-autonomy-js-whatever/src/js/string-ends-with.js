/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/string-ends-with
 */
define([
    'underscore'
], function() {
    _.mixin({
        /**
         * @desc Tests if the given string ends with any of the given suffices. Provided as an underscore mixin
         * @param {string} string The string to test
         * @param {string|String[]} suffices The suffices to test for
         * @returns {boolean} true if the given string ends with any of the given suffices, false otherwise
         * @example
         * _.endsWith('hello world', 'world'); // returns true
         * _.endsWith('hello world', 'hello'); // returns false
         * _.endsWith('hello world', ['hello', 'world']); // returns true
         */
        endsWith: function(string, suffices) {
            if(_.isString(string) && (_.isArray(suffices) || _.isString(suffices))){
                var length = string.length;

                if(!_.isArray(suffices)) {
                    suffices = [suffices];
                }

                return _.some(suffices, function(suffix) {
                    return string.indexOf(suffix, length - suffix.length) !== -1;
                });
            }

            return false;
        }
    });
});
