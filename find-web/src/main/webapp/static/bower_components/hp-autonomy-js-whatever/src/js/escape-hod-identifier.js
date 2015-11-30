/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/escape-hod-identifier
 */
define(function() {

    var regExp = /([\\:])/g;

    //noinspection UnnecessaryLocalVariableJS
    /**
     * @alias module:js-whatever/js/escape-hod-identifier
     * @desc Escape colons in HOD identifiers. HOD identifiers (domain names, application names, index names etc) can be
     * concatenated with colons. In these cases, : must become \: and \ must become \\.
     * @param {string} input The input string
     * @return {string} The escaped string
     */
    var escapeHodIdentifier = function escapeHodIdentifier(input) {
        return input.replace(regExp, '\\$&');
    };

    return escapeHodIdentifier;

});
