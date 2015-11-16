/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/regex-replace
 */
define(function(){
    /**
     * @alias module:js-whatever/js/regex-replace
     * @desc Version of 'string'.replace(regex, function(){}) which will apply noFn to the parts of the string that
     * don't match and yesFn to the parts which do match
     * @param {RegExp} regex
     * @param {string} text Text to perform replacements on
     * @param {function} yesFn Function applied to parts of the string which match regex
     * @param {function} noFn Function applied to parts of the string which do not match regex
     * @returns {string} text after the application of the appropriate functions
     * @example
     * var wrapper = function(match) {
     *     return '{' + match + '}'
     * }
     *
     * regexReplace(/\d/g, '12345abc', wrapper, _.identity); // returns '{1}{2}{3}{4}{5}abc'
     * regexReplace(/\d+/g, '12345abc', wrapper, _.identity); // returns '{12345}abc'
     * regexReplace(/\d+/g, '12345abc678', wrapper, _.identity); // returns '{12345}abc{678}'
     * regexReplace(/\d+/g, '12345abc', _.identity, wrapper); // returns '12345{abc}'
     */
    var regexReplace = function regexReplace(regex, text, yesFn, noFn) {
        if (!regex.global || !yesFn || !noFn) {
            throw new Error('regex must be global, yesFn and noFn must be defined');
        }

        if (!text) {
            return text;
        }

        var match, lastIndex = 0, output = '';

        /* jshint -W084 */
        while (match = regex.exec(text)) {
            var offset = regex.lastIndex - match[0].length;
            if (lastIndex < offset) {
                output += noFn(text.substring(lastIndex, offset));
            }
            output += yesFn.apply(this, match);
            lastIndex = regex.lastIndex;
        }
        /* jshint +W084 */

        if (lastIndex < text.length) {
            output += noFn(text.substring(lastIndex));
        }

        return output;
    };

    return regexReplace;
});