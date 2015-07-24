define([
    'backbone',
    'moment',
    'find/app/page/date/dates-filter-view',
    'find/app/model/backbone-query-model'
], function(Backbone, moment, DatesFilterView, QueryModel) {

    describe('Dates Filter View', function() {
        beforeEach(function() {
            this.queryModel = new Backbone.Model();

            this.datesFilterView = new DatesFilterView({
                queryModel: this.queryModel
            });

            this.datesFilterView.render();
        });

        describe('after initialization', function() {

            it('should not tick any date range', function() {
                var checkboxes = this.datesFilterView.$('[data-id] i:not(.checked)');

                var anyTicked = _.find(checkboxes, function(checkbox) {
                    return !$(checkbox).hasClass('hide');
                });

                expect(anyTicked).not.toBeDefined();
            });

        });

        describe('after selecting the last week checkbox', function() {
            beforeEach(function() {
                this.datesFilterView.$("[data-id='" + QueryModel.DateRange.week + "'] i").click();
            });

            it('should tick the weeks checkbox', function() {
                expect(this.datesFilterView.$("[data-id='" + QueryModel.DateRange.week + "'] i").hasClass('hide')).toBe(false);
            });

            it('should change the dateRange on the queryModel to weeks', function() {
                expect(this.queryModel.get('dateRange')).toBe(QueryModel.DateRange.week);
            });

            describe('after clicking the last week checkbox again', function() {
                beforeEach(function() {
                    this.datesFilterView.$("[data-id='" + QueryModel.DateRange.week + "'] i").click();
                });

                it('should not tick any date range', function() {
                    var checkboxes = this.datesFilterView.$("[data-id] i");
                    var anyTicked = _.find(checkboxes, function(checkbox) {
                        return !$(checkbox).hasClass('hide');
                    });
                    expect(anyTicked).toBe(undefined);
                });

                it('should set the dateRange attribute on the queryModel to "nothing" and the minDate and maxDate attributes should be null', function() {
                    expect(this.queryModel.get('dateRange')).not.toBeDefined();
                    expect(this.queryModel.get('maxDate')).toBe(undefined);
                    expect(this.queryModel.get('minDate')).toBe(undefined);
                });
            });
        });

        describe('after selecting the custom checkbox', function() {
            beforeEach(function() {
                this.datesFilterView.$("[data-id='" + QueryModel.DateRange.custom + "'] i").click();
            });

            it('should tick the custom checkbox', function() {
                expect(this.datesFilterView.$("[data-id='" + QueryModel.DateRange.custom + "'] i").hasClass('hide')).toBe(false);
            });

            it('should change the dateRange on the queryModel to weeks', function() {
                expect(this.queryModel.get('dateRange')).toBe(QueryModel.DateRange.custom);
            });

            it('should set the minDate and maxDate attributes on the queryModel to null', function() {
                expect(this.queryModel.get('maxDate')).toBe(null);
                expect(this.queryModel.get('minDate')).toBe(null);
            });

            describe('simulating a change made by the datepicker to minDate', function() {
                beforeEach(function() {
                    this.twoMonthsAgo = moment().subtract(2, 'months');
                    this.datesFilterView.setMinDate(this.twoMonthsAgo);
                });

                it('should set the minDate attribute to the moment object supplied by setMinDate on the query model', function() {
                    expect(this.queryModel.get('minDate')).toBe(this.twoMonthsAgo);
                });

                describe('clicking the Max Date icon', function() {
                    beforeEach(function() {
                        this.now = moment();
                        this.datesFilterView.setMaxDate(this.now);
                    });

                    it('should set the maxDate attribute to the moment object supplied by setMinDate on the query model', function() {
                        expect(this.queryModel.get('maxDate')).toBe(this.now);
                    });

                    describe('then selecting last month', function() {
                        beforeEach(function() {
                            this.datesFilterView.$("[data-id='" + QueryModel.DateRange.month + "'] i").click();
                        });

                        it('should tick the months checkbox', function() {
                            expect(this.datesFilterView.$("[data-id='" + QueryModel.DateRange.month + "'] i").hasClass('hide')).toBe(false);
                        });

                        it('should change the dateRange on the queryModel to month', function() {
                            expect(this.queryModel.get('dateRange')).toBe(QueryModel.DateRange.month);
                        });

                        it('should change at least the minDate attribute on the query model', function() {
                            expect(this.queryModel.get('minDate')).not.toBe(this.now);
                        });

                        describe('then selecting custom again', function() {
                            beforeEach(function() {
                                this.datesFilterView.$("[data-id='" + QueryModel.DateRange.custom + "'] i").click();
                            });

                            it('should tick the custom checkbox', function() {
                                expect(this.datesFilterView.$("[data-id='" + QueryModel.DateRange.custom + "'] i").hasClass('hide')).toBe(false);
                            });

                            it('should change the dateRange on the queryModel to weeks', function() {
                                expect(this.queryModel.get('dateRange')).toBe(QueryModel.DateRange.custom);
                            });

                            it('should set the minDate and maxDate attributes on the queryModel to null', function() {
                                expect(this.queryModel.get('maxDate')).toBe(this.now);
                                expect(this.queryModel.get('minDate')).toBe(this.twoMonthsAgo);
                            });
                        });
                    });
                });


            })
        });

        describe('after selecting the custom checkbox, setting min and max dates and then the query model having the min and max dates set to null', function() {
            beforeEach(function() {
                this.datesFilterView.$("[data-id='" + QueryModel.DateRange.custom + "']").click();
                this.datesFilterView.setMinDate(this.twoMonthsAgo);
                this.datesFilterView.setMaxDate(this.now);

                this.queryModel.set({
                    dateRange: 'custom',
                    minDate: null,
                    maxDate: null
                });
            });

            it('should clear the text from the display box', function() {
                expect(this.datesFilterView.$('.results-filter-min-date input').val()).toBe('');
                expect(this.datesFilterView.$('.results-filter-max-date input').val()).toBe('');
            });
        })
    })

});