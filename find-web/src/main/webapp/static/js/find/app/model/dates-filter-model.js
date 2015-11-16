define([
    '../../../../bower_components/backbone/backbone',
    'moment'
], function(Backbone, moment) {

    var dateRange = {
        custom: 'custom',
        year: 'year',
        month: 'month',
        week: 'week'
    };

    var dateRangeDescription = {
        year:  {maxDate: moment(), minDate: moment().subtract(1, 'years')},
        month: {maxDate: moment(), minDate: moment().subtract(1, 'months')},
        week: {maxDate: moment(), minDate: moment().subtract(1, 'weeks')}
    };

    return Backbone.Model.extend({
        defaults: {
            minDate: null,
            maxDate: null,
            dateRange: null
        },

        initialize: function(attributes, options) {
            this.queryModel = options.queryModel;

            this.listenTo(this, 'change', function() {
                this.queryModel.set({
                    minDate: this.get('minDate'),
                    maxDate: this.get('maxDate')
                })
            })
        },

        setDateRange: function(range) {
            if(range === dateRange.custom) {
                this.set({
                    minDate: this.customMinDate,
                    maxDate: this.customMaxDate,
                    dateRange: dateRange.custom
                });
            } else if(range) {
                var dateRangeProperties = dateRangeDescription[range] || {};

                this.set({
                    minDate: dateRangeProperties.minDate,
                    maxDate: dateRangeProperties.maxDate,
                    dateRange: range
                });
            } else {
                this.set({
                    minDate: null,
                    maxDate: null,
                    dateRange: null
                });
            }
        },

        setMinDate: function(date) {
            // library gives us false which would trigger a change event
            date = date || undefined;

            this.customMinDate = date;

            this.set({
                dateRange: dateRange.custom,
                minDate: date,
                maxDate: this.customMaxDate
            });
        },

        setMaxDate: function(date) {
            // library gives us false which would trigger a change event
            date = date || undefined;

            this.customMaxDate = date;

            this.set({
                dateRange: dateRange.custom,
                minDate: this.customMinDate,
                maxDate: date
            });
        }
    }, {
        dateRange: dateRange
    });
});