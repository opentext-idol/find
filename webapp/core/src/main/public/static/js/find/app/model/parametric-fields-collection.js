/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'moment',
    'find/app/model/find-base-collection'
], function(_, moment, FindBaseCollection) {
    'use strict';

    function defaultCurrentNumericRangeAttributes(absoluteRange) {
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
                currentMin: absoluteRange.min - 0.5,
                currentMax: absoluteRange.min + 0.5
            }
            // It is not possible to specify inclusive upper ranges when fetching parametric values from IDOL.
            : {
                currentMin: absoluteRange.min,
                // To display the extreme values, default to a range 1% larger than the data.
                currentMax: absoluteRange.max + 0.01 * (absoluteRange.max - absoluteRange.min)
            };
    }

    function defaultCurrentDateRangeAttributes(absoluteRange) {
        if(!absoluteRange.min) {
            absoluteRange.min = moment();
        }

        if(!absoluteRange.max) {
            absoluteRange.max = moment();
        }

        return absoluteRange.max.isSame && absoluteRange.max.isSame(absoluteRange.min)
            // The current max must always be greater than the current min for bars to be visible
            // on the widgets. If there is only one value for the field, the absolute max will equal
            // the absolute min. In this case, default to a range spanning 1 day around this value.
            ? {
                currentMin: moment(absoluteRange.min).subtract(12, 'hour').utc().milliseconds(0).format(),
                currentMax: moment(absoluteRange.max).add(12, 'hour').utc().milliseconds(0).format()
            }
            // It is not possible to specify inclusive upper ranges when fetching parametric values from IDOL.
            : {
                currentMin: moment(absoluteRange.min),
                // To display the extreme values, default to a range 1% larger than the data.
                currentMax: moment(absoluteRange.max).add(0.01 * (moment(absoluteRange.max).diff(absoluteRange.min, 'second')), 'second')//???convert min to moment?
            };
    }

    // The currentMin and currentMax attributes are the current range displayed on numeric/date widgets.
    return FindBaseCollection.extend({
        url: 'api/public/fields/parametric',

        model: FindBaseCollection.Model.extend({
            defaults: _.extend({
                totalValues: 0
            }, defaultCurrentNumericRangeAttributes({min: 0, max: 0})),

            parse: function(response) {
                return response.type === 'NumericDate'
                    ? _.defaults(defaultCurrentDateRangeAttributes(response), response)
                    : _.defaults(defaultCurrentNumericRangeAttributes(response), response);
            },

            getDefaultCurrentRange: function() {
                const absoluteRange = this.pick('min', 'max');
                return this.get('type') === 'NumericDate'
                    ? defaultCurrentDateRangeAttributes(absoluteRange)
                    : defaultCurrentNumericRangeAttributes(absoluteRange);
            }
        })
    });
});
