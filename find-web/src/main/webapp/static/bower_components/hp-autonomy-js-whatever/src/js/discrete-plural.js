/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/discrete-plural
 */
define(function() {
    //noinspection UnnecessaryLocalVariableJS
    /**
     * @typedef DiscretePluralFunction
     * @type {function}
     * @param {number} count The number of items
     * @returns {string} The empty string for non-integer numbers. The singular string for the numbers 1 and -1. The
     * plural string for all other numbers.
     */
    /**
     * @alias module:js-whatever/js/discrete-plural
     * @desc Generates a function which returns a pluralised form for a given number of items
     * @param {string} singular The singular form of the string
     * @param {string} plural The plural form of the string
     * @returns {DiscretePluralFunction} The DiscretePluralFunction for the two strings
     * @example
     * var pluralFunction = discretePlural('kitten', 'kittens');
     *
     * pluralFunction(1); // returns '1 kitten'
     * pluralFunction(-1); // returns '-1 kitten'
     * pluralFunction(3); // returns '3 kittens'
     * pluralFunction(1.5); //returns ''
     */
    var discretePlural = function(singular, plural) {
        return function(count) {
            if ((count % 1) !== 0) {
                // prevents printing of fractional documents on flot axes
                return '';
            }
            return count + (count === 1 || count === -1 ? ' ' + singular : ' ' + plural);
        };
    };

    return discretePlural;
});