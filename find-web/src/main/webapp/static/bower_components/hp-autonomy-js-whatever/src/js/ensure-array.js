/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/ensure-array
 */
define(['../../../underscore/underscore'], function(_){
    //noinspection UnnecessaryLocalVariableJS
    /**
     * @alias module:js-whatever/js/ensure-array
     * @desc Function which ensures an array is present. Useful for dealing with the results of JSON serialized
     * using the Badgerfish convention
     * @param {*} value Value to ensure is an array
     * @returns {Array} value if value is an array, a singleton array containing value for truthy values, and the
     * empty array for falsy values
     */
    var ensureArray = function(value) {
        if(_.isArray(value)) {
            return value;
        }
        else if(value) {
            return [value];
        }
        else {
            return [];
        }
    };

    return ensureArray;
});