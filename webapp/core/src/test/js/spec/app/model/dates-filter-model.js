/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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
    'moment',
    'find/app/model/dates-filter-model'
], function(moment, DatesFilterModel) {
    'use strict';

    const NOW = 1455000000000;

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

                const output = this.model.toQueryModelAttributes();
                expect(output.minDate).toBeNull();
                expect(output.maxDate.unix() * 1000).toBe(NOW);
            });

            it('returns the last week if the date range is last week', function() {
                this.model.set('dateRange', DatesFilterModel.DateRange.WEEK);

                const output = this.model.toQueryModelAttributes();
                expect(output.minDate.unix()).toBe(moment(NOW).subtract(1, 'week').unix());
                expect(output.maxDate.unix() * 1000).toBe(NOW);
            });

            it('returns the range since the last fetch if the date range is new', function() {
                this.model.set('dateRange', DatesFilterModel.DateRange.NEW);

                const output = this.model.toQueryModelAttributes();
                expect(output.minDate.unix()).toBe(moment(NOW).subtract(1, 'week').unix());
                expect(output.maxDate).toBeNull();
            });
        });
    });
});
