define([
    'backbone',
    'moment'
], function(Backbone, moment) {

    /**
     * @enum {String}
     * @readonly
     */
    var DateRange = {
        CUSTOM: 'CUSTOM',
        YEAR: 'YEAR',
        MONTH: 'MONTH',
        WEEK: 'WEEK'
    };

    return Backbone.Model.extend({
        /**
         * @typedef {Object} DateFilterModelAttributes
         * @property {?DateRange} dateRange
         * @property {?Moment} customMinDate
         * @property {?Moment} customMaxDate
         */
        /**
         * @type DateFilterModelAttributes
         */
        defaults: {
            dateRange: null,
            customMinDate: null,
            customMaxDate: null
        },

        /**
         * Convert this model to minDate and maxDate attributes for the QueryModel.
         * @return {{minDate: ?Moment, maxDate: ?Moment}}
         */
        toQueryModelAttributes: function() {
            var dateRange = this.get('dateRange');

            if (dateRange === DateRange.CUSTOM) {
                return {
                    maxDate: this.get('customMaxDate'),
                    minDate: this.get('customMinDate')
                };
            } else if (dateRange === null) {
                return {
                    maxDate: null,
                    minDate: null
                };
            } else {
                var period;

                if (dateRange === DateRange.MONTH) {
                    period = 'month';
                } else if (dateRange === DateRange.WEEK) {
                    period = 'week';
                } else {
                    period = 'year';
                }

                return {
                    minDate: moment().subtract(1, period),
                    maxDate: moment()
                };
            }
        }
    }, {
        DateRange: DateRange
    });

});