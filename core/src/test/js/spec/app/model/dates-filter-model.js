define([
    'find/app/model/dates-filter-model',
    'moment'
], function(DatesFilterModel, moment) {

    var NOW = 1455000000000;

    describe('Dates Filter Model', function() {
        beforeEach(function() {
            jasmine.clock().install();
            jasmine.clock().mockDate(new Date(NOW));

            this.model = new DatesFilterModel({
                dateRange: null,
                customMinDate: null,
                customMaxDate: moment(NOW),
                dateNewDocsLastFetched: moment(NOW).subtract(1, 'week')
            });
        });

        afterEach(function() {
            jasmine.clock().uninstall();
        });

        describe('toQueryModelAttributes function', function() {
            it('returns null for min and max dates if the date range is null', function() {
                expect(this.model.toQueryModelAttributes()).toEqual({
                    dateRange: null,
                    minDate: null,
                    maxDate: null
                });
            });

            it('returns the custom min and max dates if the date range is custom', function() {
                this.model.set('dateRange', DatesFilterModel.DateRange.CUSTOM);

                var output = this.model.toQueryModelAttributes();
                expect(output.minDate).toBeNull();
                expect(output.maxDate.unix() * 1000).toBe(NOW);
            });

            it('returns the last week if the date range is last week', function() {
                this.model.set('dateRange', DatesFilterModel.DateRange.WEEK);

                var output = this.model.toQueryModelAttributes();
                expect(output.minDate.unix()).toBe(moment(NOW).subtract(1, 'week').unix());
                expect(output.maxDate.unix() * 1000).toBe(NOW);
            });

            it('returns the range since the last fetch if the date range is new', function() {
                this.model.set('dateRange', DatesFilterModel.DateRange.NEW);

                var output = this.model.toQueryModelAttributes();
                expect(output.minDate.unix()).toBe(moment(NOW).subtract(1, 'week').unix());
                expect(output.maxDate).toBeNull();
            });
        });
    });

});