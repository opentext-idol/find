define([
    'backbone',
    'find/app/model/dates-filter-model',
    'moment'
], function(Backbone, DatesFilterModel, moment) {

    describe('Dates Filter Model', function() {
        beforeEach(function() {
            this.dateOne = 'dateOne';

            this.queryModel = new Backbone.Model();

            this.datesFilterModel = new DatesFilterModel({}, {queryModel: this.queryModel});
        });

        describe('calling setDateRange with weeks', function() {
            beforeEach(function() {
                this.datesFilterModel.setDateRange(DatesFilterModel.DateRange.WEEK);
            });

            it('should set maxDate and minDate on the queryModel to an interval spanning a week', function() {
                var minDate = this.queryModel.get('minDate');
                var maxDate = this.queryModel.get('maxDate');

                expect(moment(minDate).isBetween(
                    moment().subtract(1, 'weeks').subtract(1, 'minutes'),
                    moment().subtract(1, 'weeks').add(1, 'minutes')
                )).toBe(true);

                expect(moment(maxDate).isBetween(
                    moment().subtract(1, 'minutes'),
                    moment().add(1, 'minutes')
                )).toBe(true);
            });

            describe('calling setDateRange with custom', function() {
                beforeEach(function() {
                    this.datesFilterModel.setDateRange(DatesFilterModel.DateRange.CUSTOM);
                });

                it('should set maxDate and minDate to falsy values', function() {
                    expect(this.queryModel.get('minDate')).toBeFalsy();
                    expect(this.queryModel.get('maxDate')).toBeFalsy();
                });
            });
        });

        describe('calling setMaxDate', function() {
            beforeEach(function() {
                this.datesFilterModel.setMaxDate(this.dateOne);
            });

            it('should set the same value on the queryModel', function() {
                expect(this.queryModel.get('maxDate')).toBe(this.dateOne);
            });

            it('should set the value of its dateRange attribute to custom', function() {
                expect(this.datesFilterModel.get('dateRange')).toBe(DatesFilterModel.DateRange.CUSTOM);
            });
        });

        describe('calling setMinDate', function() {
            beforeEach(function() {
                this.datesFilterModel.setMaxDate(this.dateOne);
            });

            it('should set the same value on the queryModel', function() {
                expect(this.queryModel.get('maxDate')).toBe(this.dateOne);
            });

            it('should set the value of its dateRange attribute to custom', function() {
                expect(this.datesFilterModel.get('dateRange')).toBe(DatesFilterModel.DateRange.CUSTOM);
            });
        });

    });

});