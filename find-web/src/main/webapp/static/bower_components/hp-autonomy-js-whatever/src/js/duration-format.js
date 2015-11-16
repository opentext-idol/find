/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/duration-format
 */
define([
    '../../../underscore/underscore'
], function(_){
    /**
     * @typedef DurationFormatStrings
     * @type object
     * @property {string} [duration.day=' day']
     * @property {string} [duration.days=' days']
     * @property {string} [duration.short.hour=' hour']
     * @property {string} [duration.short.hours=' hours']
     * @property {string} [duration.short.minute=' min']
     * @property {string} [duration.short.minutes=' mins']
     * @property {string} [duration.short.seconds='s']
     * @property {string} [duration.short.millisecs='ms']
     */
    //noinspection UnnecessaryLocalVariableJS
    /**
     * @alias module:js-whatever/js/duration-format
     * @desc Rounds a duration to the specified precision. The duration is represented in terms of the largest
     * possible interval (from days, hours, minutes, seconds, milliseconds)
     * @param {number} ms The duration to format in milliseconds
     * @param {DurationFormatStrings} strings Formatting strings to use
     * @param {number} [precision=1] The number of digits to appear after the decimal point
     * @returns {string} A number to the specified precision, followed by a string representing the time period
     */
    var durationFormat = function(ms, strings, precision) {
        var oneDay = strings ? ' ' + strings['duration.day'] : ' day';
        var days = strings ? ' ' + strings['duration.days'] : ' days';
        var oneHour = strings ? ' ' + strings['duration.short.hour'] : ' hour';
        var hours = strings ? ' ' + strings['duration.short.hours'] : ' hours';
        var oneMinute = strings ? ' ' + strings['duration.short.minute'] : ' min';
        var minutes = strings ? ' ' + strings['duration.short.minutes'] : ' mins';
        var seconds = strings ? strings['duration.short.seconds'] : 's';
        var milliseconds = strings ? strings['duration.short.millisecs'] : 'ms';

        var round;

        if (_.isUndefined(precision) || _.isNull(precision)) {
            precision = 1;
        }

        if (!isFinite(ms)) {
            return ms === Infinity ? '\u221e' : ms === -Infinity ? '-\u221e' : String(ms);
        }

        var magnitude = Math.abs(ms);

        if (magnitude >= 86400e3) {
            round = ms / 86400e3;
            return Math.abs(round) === 1 ? round + oneDay : round.toFixed(precision) + days;
        }
        else if (magnitude >= 3600e3) {
            round = ms / 3600e3;
            return Math.abs(round) === 1 ? round + oneHour : round.toFixed(precision) + hours;
        }
        else if (magnitude >= 60e3) {
            round = ms / 60e3;
            return Math.abs(round) === 1 ? round + oneMinute : round.toFixed(precision) + minutes;
        }
        else if (magnitude >= 1e3) {
            return (ms / 1e3).toFixed(precision) + seconds;
        }

        return Number(ms).toFixed(precision) + milliseconds;
    };

    return durationFormat;
});
