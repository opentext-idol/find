define([
    'backbone'
], function(Backbone) {

    return Backbone.Model.extend({
        /**
         * @typedef {Object} GeographyModelAttributes
         * @property {?Array} geography
         */
        /**
         * @type GeographyModelAttributes
         */
        defaults: {
            geography: []
        },

        // TODO: implement this bit
        /**
         * Convert this model to minDate and maxDate attributes for the QueryModel.
         * @return {{minDate: ?Moment, maxDate: ?Moment}}
         */
        toQueryModelAttributes: function() {
            var dateRange = this.get('dateRange');

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
                var period;

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
        }
    });

});