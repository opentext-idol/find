/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'underscore',
    'moment',
    'find/app/model/find-base-collection'
], function(_, moment, FindBaseCollection) {
    'use strict';

    function numericRangeForQuery(absoluteRange) {
        if(!absoluteRange.min) {
            absoluteRange.min = 0;
        }

        if(!absoluteRange.max) {
            absoluteRange.max = 0;
        }

        return absoluteRange.max === absoluteRange.min
            // The current max must always be greater than the current min for bars to be visible
            // on the widgets. If there is only one value for the field, the absolute max will equal
            // the absolute min. In this case, default to a range spanning 1 around this value.
            ? {
                min: absoluteRange.min - 0.5,
                max: absoluteRange.min + 0.5
            }
            // It is not possible to specify inclusive upper ranges when fetching parametric values from IDOL.
            : {
                min: absoluteRange.min,
                // To display the extreme values, default to a range 1% larger than the data.
                max: absoluteRange.max + 0.01 * (absoluteRange.max - absoluteRange.min)
            };
    }

    function dateRangeForQuery(absoluteRange) {
        return absoluteRange.max.isSame && absoluteRange.max.isSame(absoluteRange.min)
            // The current max must always be greater than the current min for bars to be visible
            // on the widgets. If there is only one value for the field, the absolute max will equal
            // the absolute min. In this case, default to a range spanning 1 day around this value.
            ? {
                min: absoluteRange.min.subtract(12, 'hour').utc().milliseconds(0),
                max: absoluteRange.max.add(12, 'hour').utc().milliseconds(0)
            }
            // It is not possible to specify inclusive upper ranges when fetching parametric values from IDOL.
            : {
                min: absoluteRange.min,
                // To display the extreme values, default to a range 1% larger than the data.
                max: absoluteRange.max.add(0.01 * absoluteRange.max.diff(absoluteRange.min, 'second'), 'second')
            };
    }

    /**
     * Attributes:
     * @property currentMin, currentMax The current range displayed on numeric/date widgets; null if
     *                                  they haven't been modified since min and max were last
     *                                  updated
     */
    return FindBaseCollection.extend({
        url: 'api/public/fields/parametric',

        model: FindBaseCollection.Model.extend({
            defaults: {
                totalValues: 0,
                min: 0, max: 0,
                currentMin: null, currentMax: null
            },

            parse: function(response) {
                return _.defaults({ currentMin: null, currentMax: null }, response);
            },

            /**
             * Reset the currentMin and currentMax attributes to null.
             */
            resetCurrent: function () {
                this.set({ currentMin: null, currentMax: null });
            },

            /**
             * Get the current selected range (ie. currentMin/currentMax, handling null values).
             *
             * @param defaultRange Optionally override the absolute range as a { min, max } object
             *                     with number values (before overriding with currentMin/currentMax)
             * @returns Range as a { min, max } object, with Moment values for date fields
             */
            getRange: function (defaultRange) {
                const range = _.defaults({
                    min: this.get('currentMin') === null ? undefined : this.get('currentMin'),
                    max: this.get('currentMax') === null ? undefined : this.get('currentMax')
                }, defaultRange, this.pick('min', 'max'));

                return this.get('type') === 'NumericDate' ?
                    { min: moment(range.min), max: moment(range.max) } :
                    range;
            },

            /**
             * Get the current selected range (ie. currentMin/currentMax, handling null values),
             * with numeric values.
             *
             * @param defaultRange Optionally override the absolute range as a { min, max } object
             *                     with number values (before overriding with currentMin/currentMax)
             * @returns Range as a { min, max } object, with number values
             */
            getNumericRange: function (defaultRange) {
                const range = this.getRange(defaultRange);
                return this.get('type') === 'NumericDate' ?
                    { min: range.min.valueOf(), max: range.max.valueOf() } :
                    range;
            },

            /**
             * Get the range to use in query restrictions.
             *
             * @param defaultRange Optionally override the absolute range as a { min, max } object
             *                     with number values (before overriding with currentMin/currentMax)
             * @returns Range as a { min, max } object, with Moment values for date fields
             */
            getRangeForQuery: function (defaultRange) {
                const range = this.getRange(defaultRange);
                return this.get('type') === 'NumericDate' ?
                    dateRangeForQuery(range) : numericRangeForQuery(range);
            }
        })
    });
});
