define([
    'backbone',
    'moment'
], function(Backbone, moment) {

    var DateRange = {
        CUSTOM: 'CUSTOM',
        YEAR: 'YEAR',
        MONTH: 'MONTH',
        WEEK: 'WEEK'
    };

    var dateRangeDescription = {
        YEAR: {maxDate: moment(), minDate: moment().subtract(1, 'years')},
        MONTH: {maxDate: moment(), minDate: moment().subtract(1, 'months')},
        WEEK: {maxDate: moment(), minDate: moment().subtract(1, 'weeks')}
    };

    return Backbone.Model.extend({
        defaults: {
            dateRange: null
        },

        initialize: function(attributes, options) {
            this.queryModel = options.queryModel;

            this.listenTo(this, 'change', function() {
                this.queryModel.set({
                    minDate: this.get('minDate'),
                    maxDate: this.get('maxDate')
                });
            });

            this.customMinDate = this.queryModel.get('minDate') || undefined;
            this.customMaxDate = this.queryModel.get('minDate') || undefined;

            this.set({
                dateRange: (this.customMinDate || this.customMaxDate) ? DateRange.CUSTOM : null,
                minDate: this.customMinDate,
                maxDate: this.customMaxDate
            });
        },

        setDateRange: function(range) {
            if (range === DateRange.CUSTOM) {
                this.set({
                    minDate: this.customMinDate,
                    maxDate: this.customMaxDate,
                    dateRange: DateRange.CUSTOM
                });
            } else if (range) {
                var dateRangeProperties = dateRangeDescription[range] || {};

                this.set({
                    minDate: dateRangeProperties.minDate,
                    maxDate: dateRangeProperties.maxDate,
                    dateRange: range
                });
            } else {
                this.set({
                    minDate: undefined,
                    maxDate: undefined,
                    dateRange: null
                });
            }
        },

        setMinDate: function(date) {
            // library gives us false which would trigger a change event
            date = date || undefined;

            this.customMinDate = date;

            this.set({
                dateRange: DateRange.CUSTOM,
                minDate: date,
                maxDate: this.customMaxDate
            });
        },

        setMaxDate: function(date) {
            // library gives us false which would trigger a change event
            date = date || undefined;

            this.customMaxDate = date;

            this.set({
                dateRange: DateRange.CUSTOM,
                minDate: this.customMinDate,
                maxDate: date
            });
        }
    }, {
        DateRange: DateRange
    });
});