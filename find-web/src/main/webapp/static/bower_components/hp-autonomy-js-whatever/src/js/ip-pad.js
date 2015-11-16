/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/ip-pad
 */
define(function(){
    //noinspection UnnecessaryLocalVariableJS
    /**
     * @alias module:js-whatever/js/ip-pad
     * @desc Pads the components of an IPv4 address with leading zeros to allow them to be lexicographically sorted
     * @param {string} ip The IP address
     * @returns {string} The IP address with leading zeros
     * @example
     * ipPad('93.184.216.119'); // returns 093.184.216.119
     * ipPad('10.7.14.241'); // returns 010.007.014.241
     */
    var ipPad = function(ip) {
        return !ip ? ip : String(ip).replace(/\d+/g, function(num) {
            return '000'.slice(num.length) + num;
        });
    };

    return ipPad
});
