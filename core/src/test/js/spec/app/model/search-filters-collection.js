/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-testing/backbone-mock-factory',
    'find/app/model/dates-filter-model',
    'find/app/model/search-filters-collection',
    'parametric-refinement/selected-values-collection',
    'databases-view/js/databases-collection',
    'i18n!find/nls/bundle',
    'fieldtext/js/field-text-parser',
    'backbone',
    'moment'
], function(mockFactory, DatesFilterModel, FiltersCollection, SelectedParametricValues, DatabasesCollection, i18n, fieldTextParser, Backbone, moment) {

    var WOOKIEPEDIA = {
        id: 'TESTDOMAIN:wookiepedia',
        domain: 'TESTDOMAIN',
        name: 'wookiepedia'
    };

    var WIKI_ENG = {
        id: 'TESTDOMAIN:wiki_eng',
        domain: 'TESTDOMAIN',
        name: 'wiki_eng'
    };

    var INITIAL_MIN_DATE = moment();
    var DATE_FORMAT = 'LLL';

    describe('Search filters collection initialised with an indexes filter, a DatesFilterModel with a min date set and a selected parametric value on the AGE field', function() {
        beforeEach(function() {
            this.indexesCollection = new DatabasesCollection([WOOKIEPEDIA, WIKI_ENG]);
            this.selectedIndexesCollection = new DatabasesCollection([WIKI_ENG]);

            this.queryModel = new Backbone.Model({
                minDate: INITIAL_MIN_DATE
            });

            this.datesFilterModel = new Backbone.Model({
                dateRange: DatesFilterModel.DateRange.CUSTOM,
                customMinDate: INITIAL_MIN_DATE
            });

            this.selectedParametricValues = new SelectedParametricValues([
                {field: 'AGE', value: '4'}
            ]);

            this.collection = new FiltersCollection([], {
                queryModel: this.queryModel,
                indexesCollection: this.indexesCollection,
                queryState: {
                    datesFilterModel: this.datesFilterModel,
                    selectedIndexes: this.selectedIndexesCollection,
                    selectedParametricValues: this.selectedParametricValues
                }
            });
        });

        it('contains three models', function() {
            expect(this.collection.length).toBe(3);
        });

        it('contains a min date filter model', function() {
            var model = this.collection.get(FiltersCollection.FilterType.MIN_DATE);
            expect(model).toBeDefined();
            expect(model.get('text')).toContain(INITIAL_MIN_DATE.format(DATE_FORMAT));
        });

        it('contains a databases filter model', function() {
            var model = this.collection.get(FiltersCollection.FilterType.INDEXES);
            expect(model).toBeDefined();
            expect(model.get('text')).toContain(WIKI_ENG.name);
            expect(model.get('text')).not.toContain(WOOKIEPEDIA.name);
        });

        it('contains an AGE parametric field filter model', function() {
            var model = this.collection.findWhere({type: FiltersCollection.FilterType.PARAMETRIC});
            expect(model).toBeDefined();
            expect(model.get('field')).toBe('AGE');
            expect(model.get('text')).toContain('4');
        });

        describe('after datesFilterModel has a maxDate set', function() {
            beforeEach(function() {
                this.maxDate = moment(INITIAL_MIN_DATE).add(2, 'days');

                this.queryModel.set('maxDate', this.maxDate);

                this.datesFilterModel.set({
                    dateRange: DatesFilterModel.DateRange.CUSTOM,
                    customMaxDate: this.maxDate
                });
            });

            it('contains four models', function() {
                expect(this.collection.length).toBe(4);
            });

            it('contains a max date filter model with the correct date', function() {
                var model = this.collection.get(FiltersCollection.FilterType.MAX_DATE);
                expect(model).toBeDefined();
                expect(model.get('text')).toContain(i18n['app.until']);
                expect(model.get('text')).toContain(moment(this.maxDate).format(DATE_FORMAT));
            });
        });

        describe('after datesFilterModel has a minDate set', function() {
            beforeEach(function() {
                this.minDate = moment(INITIAL_MIN_DATE).subtract(2, 'days');

                this.datesFilterModel.set({
                    dateRange: DatesFilterModel.DateRange.CUSTOM,
                    customMinDate: this.minDate
                });

                this.queryModel.set('minDate', this.minDate);
            });

            it('contains three models', function() {
                expect(this.collection.length).toBe(3);
            });

            it('updates the min date filter model', function() {
                var model = this.collection.get(FiltersCollection.FilterType.MIN_DATE);
                expect(model.get('text')).toContain(i18n['app.from']);
                expect(model.get('text')).toContain(moment(this.minDate).format(DATE_FORMAT));
            });
        });

        describe('after datesFilterModel has dateRange set to WEEK', function() {
            beforeEach(function() {
                this.datesFilterModel.set('dateRange', DatesFilterModel.DateRange.WEEK);
            });

            it('contains three models', function() {
                expect(this.collection.length).toBe(3);
            });

            it('removes the min date model', function() {
                expect(this.collection.get(FiltersCollection.FilterType.MIN_DATE)).toBeUndefined();
            });

            it('adds a date range model', function() {
                var model = this.collection.get(FiltersCollection.FilterType.DATE_RANGE);
                expect(model.get('text')).toContain(i18n['search.dates.timeInterval.' + DatesFilterModel.DateRange.WEEK]);
            });
        });

        describe('after datesFilterModel has dateRange set to null', function() {
            beforeEach(function() {
                this.datesFilterModel.set('dateRange', null);
            });

            it('contains two models', function() {
                expect(this.collection.length).toBe(2);
            });

            it('removes the min date model', function() {
                expect(this.collection.get(FiltersCollection.FilterType.MIN_DATE)).toBeUndefined();
            });
        });

        describe('after the min date filter model is removed', function() {
            beforeEach(function() {
                this.collection.remove(FiltersCollection.FilterType.MIN_DATE);
            });

            it('sets the datesFilterModel customMinDate attribute to null', function() {
                expect(this.datesFilterModel.get('customMinDate')).toBeNull();
            });
        });

        describe('after all databases are selected', function() {
            beforeEach(function() {
                this.selectedIndexesCollection.set([WOOKIEPEDIA, WIKI_ENG]);
            });

            it('contains two models', function() {
                expect(this.collection.length).toBe(2);
            });

            it('removes the databases filter model', function() {
                expect(this.collection.get(FiltersCollection.FilterType.INDEXES)).toBeUndefined();
            });

            describe('then a database is deselected', function() {
                beforeEach(function() {
                    this.selectedIndexesCollection.set([WOOKIEPEDIA]);
                });

                it('contains three models', function() {
                    expect(this.collection.length).toBe(3);
                });

                it('adds a databases filter model', function() {
                    var model = this.collection.get(FiltersCollection.FilterType.INDEXES);
                    expect(model).toBeDefined();
                    expect(model.get('text')).toContain(WOOKIEPEDIA.name);
                    expect(model.get('text')).not.toContain(WIKI_ENG.name);
                });
            });
        });

        describe('after datesFilterModel has a minDate set', function() {
            beforeEach(function() {
                this.datesFilterModel.set({
                    dateRange: null,
                    minDate: null
                });

                this.queryModel.set('minDate', null);
            });

            it('sets the request model minDate attribute to null', function() {
                expect(this.queryModel.get('minDate')).toBeNull();
            });
        });

        describe('after the parametric filter model is removed', function() {
            beforeEach(function() {
                this.collection.remove(this.collection.where({type: FiltersCollection.FilterType.PARAMETRIC}));
            });

            it('removes the associated model from the selected parametric values collection', function() {
                expect(this.selectedParametricValues.length).toBe(0);
            });
        });

        describe('after the indexes filter model is removed', function() {
            beforeEach(function() {
                this.collection.remove(this.collection.where({type: FiltersCollection.FilterType.INDEXES}));
            });

            it('selects all of the indexes', function() {
                expect(this.selectedIndexesCollection.length).toBe(2);
            });
        });

        describe('after two more parametric values are selected from the NAME field', function() {
            beforeEach(function() {
                this.selectedParametricValues.add([
                    {field: 'NAME', fieldDisplayName: 'Name', value: 'bobby'},
                    {field: 'NAME', fieldDisplayName: 'Name', value: 'penny'}
                ]);
            });

            it('contains four models', function() {
                expect(this.collection.length).toBe(4);
            });

            it('contains a NAME parametric filter model', function() {
                var model = this.collection.findWhere({type: FiltersCollection.FilterType.PARAMETRIC,  field: 'NAME'});
                expect(model).toBeDefined();
                expect(model.get('text')).toContain('bobby');
                expect(model.get('text')).toContain('penny');
            });

            describe('then one of the NAME values is deselected', function() {
                beforeEach(function() {
                    this.selectedParametricValues.remove(this.selectedParametricValues.findWhere({field: 'NAME', value: 'penny'}));
                });

                it('contains four models', function() {
                    expect(this.collection.length).toBe(4);
                });

                it('removes the deselected field value from the NAME parametric filter model', function() {
                    var model = this.collection.findWhere({type: FiltersCollection.FilterType.PARAMETRIC, field: 'NAME'});
                    expect(model.get('text')).toContain('bobby');
                    expect(model.get('text')).not.toContain('penny');
                });
            });

            describe('then the AGE parametric value is deselected', function() {
                beforeEach(function() {
                    this.selectedParametricValues.remove(this.selectedParametricValues.findWhere({field: 'AGE', value: '4'}));
                });

                it('contains three models', function() {
                    expect(this.collection.length).toBe(3);
                });

                it('removes the AGE filter model', function() {
                    expect(this.collection.findWhere({type: FiltersCollection.FilterType.PARAMETRIC, field: 'AGE'})).toBeUndefined();
                });
            });

            describe('then the selected parametric values collection is reset with a new selected field', function() {
                beforeEach(function() {
                    this.selectedParametricValues.reset([
                        {field: 'VEHICLE', value: 'car'}
                    ]);
                });

                it('contains three models', function() {
                    expect(this.collection.length).toBe(3);
                });

                it('replaces all parametric filter models with one representing the new field', function() {
                    expect(this.collection.findWhere({type: FiltersCollection.FilterType.PARAMETRIC, field: 'AGE'})).toBeUndefined();
                    expect(this.collection.findWhere({type: FiltersCollection.FilterType.PARAMETRIC, field: 'NAME'})).toBeUndefined();

                    var vehicleModel = this.collection.findWhere({type: FiltersCollection.FilterType.PARAMETRIC, field: 'VEHICLE'});
                    expect(vehicleModel).toBeDefined();
                    expect(vehicleModel.get('text')).toContain('car');
                });
            });

            describe('then the AGE filter model is removed', function() {
                beforeEach(function() {
                    this.collection.remove(this.collection.findWhere({type: FiltersCollection.FilterType.PARAMETRIC, field: 'AGE'}));
                });

                it('contains three models', function() {
                    expect(this.collection.length).toBe(3);
                });

                it('still contains the NAME parametric filter model', function() {
                    expect(this.collection.findWhere({type: FiltersCollection.FilterType.PARAMETRIC, field: 'NAME'})).toBeDefined();
                });
            });
        });
    });

});
