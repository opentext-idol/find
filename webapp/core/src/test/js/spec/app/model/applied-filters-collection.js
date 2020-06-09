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
    'backbone',
    'js-testing/backbone-mock-factory',
    'find/app/model/dates-filter-model',
    'find/app/model/geography-model',
    'find/app/model/document-selection-model',
    'find/app/model/applied-filters-collection',
    'parametric-refinement/selected-values-collection',
    'i18n!find/nls/bundle',
    'fieldtext/js/field-text-parser',
    'find/app/util/database-name-resolver',
    'moment'
], function(Backbone, mockFactory, DatesFilterModel, GeographyModel, DocumentSelectionModel, FiltersCollection, SelectedParametricValues,
            i18n, fieldTextParser, databaseNameResolver, moment) {
    'use strict';

    const WOOKIEPEDIA = {
        id: 'TESTDOMAIN:wookiepedia',
        domain: 'TESTDOMAIN',
        name: 'wookiepedia'
    };

    const WIKI_ENG = {
        id: 'TESTDOMAIN:wiki_eng',
        domain: 'TESTDOMAIN',
        name: 'wiki_eng'
    };

    const INITIAL_MIN_DATE = moment();
    const DATE_FORMAT = 'LLL';

    describe('AppliedFiltersCollection', function() {
        beforeEach(function() {
            this.indexesCollection = new Backbone.Collection([WOOKIEPEDIA, WIKI_ENG]);
            this.selectedIndexesCollection = new Backbone.Collection([WIKI_ENG]);
            databaseNameResolver.getDatabaseDisplayNameFromDatabaseModel.and.callFake(function() {
                return WIKI_ENG.name;
            });

            this.queryModel = new Backbone.Model({
                minDate: INITIAL_MIN_DATE
            });

            this.datesFilterModel = new Backbone.Model({
                dateRange: DatesFilterModel.DateRange.CUSTOM,
                customMinDate: INITIAL_MIN_DATE
            });

            this.geographyModel = new GeographyModel({})

            this.documentSelectionModel = new DocumentSelectionModel();

            this.selectedParametricValues = new SelectedParametricValues([
                {field: 'AGE', displayName: 'Age', value: '4', displayValue: '4', type: 'Parametric'}
            ]);

            this.collection = new FiltersCollection([], {
                queryModel: this.queryModel,
                indexesCollection: this.indexesCollection,
                queryState: {
                    datesFilterModel: this.datesFilterModel,
                    geographyModel: this.geographyModel,
                    documentSelectionModel: this.documentSelectionModel,
                    selectedIndexes: this.selectedIndexesCollection,
                    selectedParametricValues: this.selectedParametricValues
                }
            });
        });

        it('contains three models', function() {
            expect(this.collection.length).toBe(3);
        });

        it('contains a min date filter model', function() {
            const model = this.collection.get(FiltersCollection.FilterType.MIN_DATE);
            expect(model).toBeDefined();
            expect(model.get('text')).toContain(INITIAL_MIN_DATE.format(DATE_FORMAT));
        });

        it('contains a databases filter model', function() {
            const model = this.collection.get(FiltersCollection.FilterType.INDEXES);
            expect(model).toBeDefined();
            expect(model.get('text')).toContain(WIKI_ENG.name);
            expect(model.get('text')).not.toContain(WOOKIEPEDIA.name);
        });

        it('contains an AGE parametric field filter model', function() {
            const model = this.collection.findWhere({type: FiltersCollection.FilterType.PARAMETRIC});
            expect(model).toBeDefined();
            expect(model.get('field')).toBe('AGE');
            expect(model.get('text')).toContain('4');
        });

        it('sets the prettified field name for the heading of the parametric field', function() {
            const model = this.collection.findWhere({type: FiltersCollection.FilterType.PARAMETRIC});
            expect(model.get('heading')).toBe('Age');
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
                const model = this.collection.get(FiltersCollection.FilterType.MAX_DATE);
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
                const model = this.collection.get(FiltersCollection.FilterType.MIN_DATE);
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
                const model = this.collection.get(FiltersCollection.FilterType.DATE_RANGE);
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
                    databaseNameResolver.getDatabaseDisplayNameFromDatabaseModel.and.callFake(function() {
                        return WOOKIEPEDIA.name;
                    });
                    this.selectedIndexesCollection.set([WOOKIEPEDIA]);
                });

                it('contains three models', function() {
                    expect(this.collection.length).toBe(3);
                });

                it('adds a databases filter model', function() {
                    const model = this.collection.get(FiltersCollection.FilterType.INDEXES);
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
                databaseNameResolver.getDatabaseInfoFromCollection.and.callFake(function() {
                    return [WOOKIEPEDIA, WIKI_ENG];
                });
                this.collection.remove(this.collection.where({type: FiltersCollection.FilterType.INDEXES}));
            });

            it('selects all of the indexes', function() {
                expect(this.selectedIndexesCollection.length).toBe(2);
            });
        });

        describe('after a document is excluded', function () {

            beforeEach(function () {
                this.documentSelectionModel.exclude('a');
            });

            it('contains four models', function () {
                expect(this.collection.length).toBe(4);
            });

            it('contains a document selection model', function () {
                const model = this.collection.get(FiltersCollection.FilterType.DOCUMENT_SELECTION);
                expect(model).toBeTruthy();
                expect(model.get('text')).toContain('Documents excluded: 1');
            });

            describe('then the document is selected again', function () {

                beforeEach(function () {
                    this.documentSelectionModel.select('a');
                });

                it('contains three models', function () {
                    expect(this.collection.length).toBe(3);
                });

                it('does not contain a document selection model', function () {
                    expect(this.collection.get(FiltersCollection.FilterType.DOCUMENT_SELECTION))
                        .toBeUndefined();
                });

            });

            describe('then another document is excluded', function () {

                beforeEach(function () {
                    this.documentSelectionModel.exclude('b');
                });

                it('updates the filter description', function () {
                    const model = this.collection.get(
                        FiltersCollection.FilterType.DOCUMENT_SELECTION);
                    expect(model).toBeTruthy();
                    expect(model.get('text')).toContain('Documents excluded: 2');
                });

            });

            describe('then the filter is removed', function () {

                beforeEach(function () {
                    this.collection.remove(this.collection.where(
                        { type: FiltersCollection.FilterType.DOCUMENT_SELECTION }));
                });

                it('contains three models', function () {
                    expect(this.collection.length).toBe(3);
                });

                it('does not contain a document selection model', function () {
                    expect(this.collection.get(FiltersCollection.FilterType.DOCUMENT_SELECTION))
                        .toBeUndefined();
                });

                it('selects the excluded document', function () {
                    expect(this.documentSelectionModel.isSelected('a')).toBe(true);
                });

            });

        });

        describe('after adding a selected parametric value with a displayName in the configuration', function() {
            beforeEach(function() {
                this.selectedParametricValues.add([
                    {
                        field: 'FELINES',
                        displayName: 'cats',
                        value: 'MR_MISTOFFELEES',
                        displayValue: 'Mr. Mistoffelees, the magical cat',
                        type: 'Parametric'
                    }
                ]);
            });

            it('uses the display names from the configuration', function() {
                const model = this.collection.findWhere({
                    type: FiltersCollection.FilterType.PARAMETRIC,
                    field: 'FELINES'
                });
                expect(model).toBeDefined();
                expect(model.get('text')).toContain('Mr. Mistoffelees, the magical cat');
            });
        });

        describe('after two more parametric values are selected from the NAME field', function() {
            beforeEach(function() {
                this.selectedParametricValues.add([
                    {field: 'NAME', displayName: 'Name', value: 'bobby', displayValue: 'bobby', type: 'Parametric'},
                    {field: 'NAME', displayName: 'Name', value: 'penny', displayValue: 'penny', type: 'Parametric'}
                ]);
            });

            it('contains four models', function() {
                expect(this.collection.length).toBe(4);
            });

            it('contains a NAME parametric filter model', function() {
                const model = this.collection.findWhere({type: FiltersCollection.FilterType.PARAMETRIC, field: 'NAME'});
                expect(model).toBeDefined();
                expect(model.get('text')).toContain('bobby');
                expect(model.get('text')).toContain('penny');
            });

            describe('then one of the NAME values is deselected', function() {
                beforeEach(function() {
                    this.selectedParametricValues.remove(this.selectedParametricValues.findWhere({
                        field: 'NAME',
                        value: 'penny'
                    }));
                });

                it('contains four models', function() {
                    expect(this.collection.length).toBe(4);
                });

                it('removes the deselected field value from the NAME parametric filter model', function() {
                    const model = this.collection.findWhere({
                        type: FiltersCollection.FilterType.PARAMETRIC,
                        field: 'NAME'
                    });
                    expect(model.get('text')).toContain('bobby');
                    expect(model.get('text')).not.toContain('penny');
                });
            });

            describe('then the AGE parametric value is deselected', function() {
                beforeEach(function() {
                    this.selectedParametricValues.remove(this.selectedParametricValues.findWhere({
                        field: 'AGE',
                        value: '4'
                    }));
                });

                it('contains three models', function() {
                    expect(this.collection.length).toBe(3);
                });

                it('removes the AGE filter model', function() {
                    expect(this.collection.findWhere({
                        type: FiltersCollection.FilterType.PARAMETRIC,
                        field: 'AGE'
                    })).toBeUndefined();
                });
            });

            describe('then the selected parametric values collection is reset with a new selected field', function() {
                beforeEach(function() {
                    this.selectedParametricValues.reset([
                        {
                            field: 'VEHICLE',
                            displayName: 'Vehicle',
                            value: 'car',
                            displayValue: 'car',
                            type: 'Parametric'
                        }
                    ]);
                });

                it('contains three models', function() {
                    expect(this.collection.length).toBe(3);
                });

                it('replaces all parametric filter models with one representing the new field', function() {
                    expect(this.collection.findWhere({
                        type: FiltersCollection.FilterType.PARAMETRIC,
                        field: 'AGE'
                    })).toBeUndefined();
                    expect(this.collection.findWhere({
                        type: FiltersCollection.FilterType.PARAMETRIC,
                        field: 'NAME'
                    })).toBeUndefined();

                    const vehicleModel = this.collection.findWhere({
                        type: FiltersCollection.FilterType.PARAMETRIC,
                        field: 'VEHICLE'
                    });
                    expect(vehicleModel).toBeDefined();
                    expect(vehicleModel.get('text')).toContain('car');
                });
            });

            describe('then the AGE filter model is removed', function() {
                beforeEach(function() {
                    this.collection.remove(this.collection.findWhere({
                        type: FiltersCollection.FilterType.PARAMETRIC,
                        field: 'AGE'
                    }));
                });

                it('contains three models', function() {
                    expect(this.collection.length).toBe(3);
                });

                it('still contains the NAME parametric filter model', function() {
                    expect(this.collection.findWhere({
                        type: FiltersCollection.FilterType.PARAMETRIC,
                        field: 'NAME'
                    })).toBeDefined();
                });
            });
        });
    });
});
