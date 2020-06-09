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
    'backbone',
    'moment'
], function(Backbone, moment) {
    'use strict';

    /**
     * @enum {String}
     * @readonly
     */
    const DateRange = {
        CUSTOM: 'CUSTOM',
        YEAR: 'YEAR',
        MONTH: 'MONTH',
        WEEK: 'WEEK',
        NEW: 'NEW'
    };

    return Backbone.Model.extend({
        /**
         * @typedef {Object} DateFilterModelAttributes
         * @property {?DateRange} dateRange
         * @property {?Moment} customMinDate
         * @property {?Moment} customMaxDate
         * @property {?Moment} dateNewDocsLastFetched
         */
        /**
         * @type DateFilterModelAttributes
         */
        defaults: {
            dateRange: null,
            customMinDate: null,
            customMaxDate: null,
            dateNewDocsLastFetched: null
        },

        /**
         * Convert this model to minDate and maxDate attributes for the QueryModel.
         * @return {{minDate: ?Moment, maxDate: ?Moment}}
         */
        toQueryModelAttributes: function() {
            const dateRange = this.get('dateRange');

            if (dateRange === DateRange.CUSTOM) {
                return {
                    dateRange: dateRange,
                    maxDate: this.get('customMaxDate'),
                    minDate: this.get('customMinDate')
                };
            } else if (dateRange === DateRange.NEW) {
                return {
                    dateRange: dateRange,
                    maxDate: null,
                    minDate: this.get('dateNewDocsLastFetched')
                }
            } else if (dateRange === null) {
                return {
                    dateRange: dateRange,
                    maxDate: null,
                    minDate: null
                };
            } else {
                let period;

                if (dateRange === DateRange.MONTH) {
                    period = 'month';
                } else if (dateRange === DateRange.WEEK) {
                    period = 'week';
                } else {
                    period = 'year';
                }

                return {
                    dateRange: dateRange,
                    minDate: moment().subtract(1, period),
                    maxDate: moment()
                };
            }
        },

        resetDateLastFetched: function() {
            this.set('dateNewDocsLastFetched', null);
            if(this.get('dateRange') === DateRange.NEW) {
                this.set('dateRange', null);
            }
        }
    }, {
        DateRange: DateRange
    });
});
