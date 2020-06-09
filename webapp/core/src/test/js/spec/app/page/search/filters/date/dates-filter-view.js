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
    'moment',
    'find/app/page/search/filters/date/dates-filter-view',
    'find/app/model/dates-filter-model'
], function(Backbone, moment, DatesFilterView, DatesFilterModel) {
    'use strict';

    describe('Dates Filter View', function() {
        beforeEach(function() {
            this.now = moment.utc(1000000000000);
            this.twoMonthsAgo = moment(this.now).subtract(2, 'months');
            this.datesFilterModel = new Backbone.Model();
            this.savedSearchModel = new Backbone.Model();

            spyOn(this.savedSearchModel, 'sync');
            spyOn(this.savedSearchModel, 'isNew');
        });

        describe('after initialisation with an unsaved search', function() {
            beforeEach(function() {
                this.savedSearchModel = new Backbone.Model();

                this.view = new DatesFilterView({
                    datesFilterModel: this.datesFilterModel,
                    savedSearchModel: this.savedSearchModel
                });
            });

            it('does not show the "Since Last Time" option', function() {
                expect(this.view.$('[data-filter-id="' + DatesFilterModel.DateRange.NEW + '"]')).toHaveLength(0);
            });
        });

        describe('after initialisation with a custom date range selected', function() {
            beforeEach(function() {
                this.datesFilterModel.set({
                    dateRange: DatesFilterModel.DateRange.CUSTOM,
                    customMinDate: null,
                    customMaxDate: this.twoMonthsAgo
                });

                this.view = new DatesFilterView({
                    datesFilterModel: this.datesFilterModel,
                    savedSearchModel: this.savedSearchModel
                });
                this.view.render();
            });

            it('only ticks the "Custom" option', function() {
                const $tickedItems = this.view.$('[data-filter-id] i:not(.hide)');
                expect($tickedItems).toHaveLength(1);
                expect($tickedItems.closest('[data-filter-id]')).toHaveAttr('data-filter-id', DatesFilterModel.DateRange.CUSTOM);
            });

            it('shows the min and max date inputs', function() {
                expect(this.view.$('.search-dates-wrapper')).not.toHaveClass('hide');
            });

            describe('then the dates filter model date range is set to null', function() {
                beforeEach(function() {
                    this.datesFilterModel.set({
                        customMinDate: moment(),
                        customMaxDate: moment(),
                        dateRange: null
                    });
                });

                it('un-ticks all date ranges', function() {
                    const $tickedItems = this.view.$('[data-filter-id] i:not(.hide)');
                    expect($tickedItems).toHaveLength(0);
                });

                it('hides the min and max date inputs', function() {
                    expect(this.view.$('.search-dates-wrapper')).toHaveClass('hide');
                });
            });
        });

        describe('after initialisation with "Last Week" selected', function() {
            beforeEach(function() {
                this.datesFilterModel.set({
                    dateRange: DatesFilterModel.DateRange.WEEK,
                    customMinDate: null,
                    customMaxDate: null
                });

                this.view = new DatesFilterView({
                    datesFilterModel: this.datesFilterModel,
                    savedSearchModel: this.savedSearchModel
                });
                this.view.render();
            });

            it('only ticks the "Last Week" option', function() {
                const $tickedItems = this.view.$('[data-filter-id] i:not(.hide)');
                expect($tickedItems).toHaveLength(1);
                expect($tickedItems.closest('[data-filter-id]')).toHaveAttr('data-filter-id', DatesFilterModel.DateRange.WEEK);
            });

            it('does not show the min and max date inputs', function() {
                expect(this.view.$('.search-dates-wrapper')).toHaveClass('hide');
            });

            describe('then the dates filter model date range is set to null', function() {
                beforeEach(function() {
                    this.datesFilterModel.set({
                        customMinDate: moment(),
                        customMaxDate: moment(),
                        dateRange: null
                    });
                });

                it('un-ticks all date ranges', function() {
                    const $tickedItems = this.view.$('[data-filter-id] i:not(.hide)');
                    expect($tickedItems).toHaveLength(0);
                });
            });
        });

        describe('after initialisation with no filter selected', function() {
            beforeEach(function() {
                this.datesFilterModel.set({
                    dateRange: null,
                    customMinDate: null,
                    customMaxDate: null
                });

                this.view = new DatesFilterView({
                    datesFilterModel: this.datesFilterModel,
                    savedSearchModel: this.savedSearchModel
                });
                this.view.render();
            });

            it('does not tick any date range', function() {
                const $tickedItems = this.view.$('[data-filter-id] i:not(.hide)');
                expect($tickedItems).toHaveLength(0);
            });

            it('does not show the min and max date inputs', function() {
                expect(this.view.$('.search-dates-wrapper')).toHaveClass('hide');
            });

            describe('then the dates filter model date range is set to "Last Week"', function() {
                beforeEach(function() {
                    this.datesFilterModel.set({
                        customMinDate: moment(),
                        customMaxDate: moment(),
                        dateRange: DatesFilterModel.DateRange.WEEK
                    });
                });

                it('only ticks the "Last Week" option', function() {
                    const $tickedItems = this.view.$('[data-filter-id] i:not(.hide)');
                    expect($tickedItems).toHaveLength(1);
                    expect($tickedItems.closest('[data-filter-id]')).toHaveAttr('data-filter-id', DatesFilterModel.DateRange.WEEK);
                });
            });

            describe('then the user clicks the "Last Week" option', function() {
                beforeEach(function() {
                    this.view.$('[data-filter-id="' + DatesFilterModel.DateRange.WEEK + '"]').click();
                });

                it('sets the date range attribute on the dates filter model to "Last "Week"', function() {
                    expect(this.datesFilterModel.get('dateRange')).toBe(DatesFilterModel.DateRange.WEEK);
                });

                describe('then the user clicks the "Last Week" option again', function() {
                    beforeEach(function() {
                        this.view.$('[data-filter-id="' + DatesFilterModel.DateRange.WEEK + '"]').click();
                    });

                    it('sets the date range attribute on the dates filter model to null', function() {
                        expect(this.datesFilterModel.get('dateRange')).toBeNull();
                    });
                });
            });

            describe('then the dates filter model date range is set to "Since Last Time"', function() {
                beforeEach(function() {
                    this.datesFilterModel.set({
                        customMinDate: moment(),
                        customMaxDate: moment(),
                        dateRange: DatesFilterModel.DateRange.NEW
                    });
                });

                it('only ticks the "Since Last Time" option', function() {
                    const $tickedItems = this.view.$('[data-filter-id] i:not(.hide)');
                    expect($tickedItems).toHaveLength(1);
                    expect($tickedItems.closest('[data-filter-id]')).toHaveAttr('data-filter-id', DatesFilterModel.DateRange.NEW);
                });

                it('attempts to sync the saved search model to the server', function() {
                    expect(this.savedSearchModel.sync).toHaveBeenCalled();
                });
            });

            describe('then the user clicks the "Since Last Time" option', function() {
                beforeEach(function() {
                    this.view.$('[data-filter-id="' + DatesFilterModel.DateRange.NEW + '"]').click();
                });

                it('sets the date range attribute on the dates filter model to "Since Last Time"', function() {
                    expect(this.datesFilterModel.get('dateRange')).toBe(DatesFilterModel.DateRange.NEW);
                });
            });
        });
    });
});
