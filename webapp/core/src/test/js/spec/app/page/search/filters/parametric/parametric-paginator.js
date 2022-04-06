/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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
    'underscore',
    'jquery',
    'backbone',
    'find/app/page/search/filters/parametric/parametric-paginator'
], function(_, $, Backbone, ParametricPaginator) {
    'use strict';

    const ALL_INDEXES = ['Broadcast', 'Generic'];

    function checkState(partialExpectedState) {
        const expectedState = _.extend({
            empty: false,
            loading: false,
            error: null
        }, partialExpectedState);

        return function() {
            expect(this.paginator.stateModel.attributes).toEqual(expectedState);
        };
    }

    function checkModels(expectedAttributes) {
        return function() {
            expect(this.paginator.valuesCollection.map(_.property('attributes'))).toEqual(expectedAttributes);
        };
    }

    function fetchNext() {
        this.paginator.fetchNext();
    }

    function respond(requestIndex, response) {
        return function() {
            this.fetchPromises[requestIndex].resolve(response);
        };
    }

    describe('ParametricPaginator', function() {
        beforeEach(function() {
            this.fetchPromises = [];
            this.fetchFunction = jasmine.createSpy('fetchFunction');

            this.selectedValues = new Backbone.Collection([
                {
                    field: 'CATEGORY',
                    displayName: 'Category',
                    value: 'ANIMALS',
                    displayValue: 'Animals',
                    type: 'Parametric'
                },
                {
                    field: 'THINGS',
                    displayName: 'Things',
                    value: 'ANIMALS',
                    displayValue: 'Animals',
                    type: 'Parametric'
                }
            ]);

            this.fetchFunction.and.callFake(function() {
                const promise = $.Deferred();
                this.fetchPromises.push(promise);
                return promise;
            }.bind(this));

            this.paginator = new ParametricPaginator({
                fetchRestrictions: {queryText: 'cat', databases: ['Generic']},
                fetchFunction: this.fetchFunction,
                fieldName: 'CATEGORY',
                fieldDisplayName: 'Category',
                allIndexes: ALL_INDEXES,
                pageSize: 2,
                selectedValues: this.selectedValues
            });
        });

        it('constructs with no error and loading but not empty', checkState({loading: true}));

        it('does not fetch on construction', function() {
            expect(this.fetchPromises.length).toBe(0);
        });

        it('constructs with an empty values collection', function() {
            expect(this.paginator.valuesCollection.length).toBe(0);
        });

        describe('when fetchNext is called', function() {
            beforeEach(fetchNext);

            it('calls the fetchFunction for page 1 with the query restrictions', function() {
                expect(this.fetchPromises.length).toBe(1);

                expect(this.fetchFunction.calls.argsFor(0)).toEqual([
                    {start: 1, maxValues: 2, fieldNames: ['CATEGORY'], queryText: 'cat', databases: ['Generic']}
                ]);
            });

            it('is loading and has no error', checkState({loading: true}));

            describe('when fetchNext is called before the request succeeds', function() {
                beforeEach(fetchNext);

                it('does not call the fetchFunction again', function() {
                    expect(this.fetchPromises.length).toBe(1);
                });

                it('is still loading and has no error', checkState({loading: true}));
            });

            describe('then the fetch succeeds with no values', function() {
                beforeEach(respond(0, {totalValues: 0, values: []}));

                it('has no error and is still loading', checkState({
                    loading: true
                }));

                it('calls the fetchFunction for page 1 without the query restrictions', function() {
                    expect(this.fetchPromises.length).toBe(2);

                    expect(this.fetchFunction.calls.argsFor(1)).toEqual([
                        {start: 1, maxValues: 2, fieldNames: ['CATEGORY'], databases: ALL_INDEXES}
                    ]);
                });

                describe('then the fetch succeeds with no values', function() {
                    beforeEach(respond(1, {totalValues: 0, values: []}));

                    it('has no error and is not loading but is empty', checkState({
                        empty: true
                    }));

                    it('makes no further requests', function() {
                        expect(this.fetchPromises.length).toBe(2);
                    });
                });

                describe('then the fetch succeeds with 2 values and a total values of 4', function() {
                    beforeEach(respond(1, {
                        totalValues: 4,
                        values: [
                            {value: 'PLANTS', displayValue: 'Plants', count: 10},
                            {value: 'ANIMALS', displayValue: 'Animals', count: 7}
                        ]
                    }));

                    it('is not loading or empty and has no error', checkState({}));

                    it('adds new models to the values collection with count 0, setting the selected flag as appropriate', checkModels([
                        {value: 'PLANTS', displayValue: 'Plants', count: 0, selected: false},
                        {value: 'ANIMALS', displayValue: 'Animals', count: 0, selected: true}
                    ]));
                });
            });

            describe('then the fetch succeeds with 1 value and a total values of 1', function() {
                beforeEach(respond(0, {
                    totalValues: 1,
                    values: [
                        {value: 'PLANTS', displayValue: 'Plants', count: 5}
                    ]
                }));

                it('has no error and is still loading', checkState({
                    loading: true
                }));

                it('adds new models to the values collection, setting the selected flag as appropriate', checkModels([
                    {value: 'PLANTS', displayValue: 'Plants', count: 5, selected: false}
                ]));

                it('calls the fetchFunction for page 1 without the query restrictions', function() {
                    expect(this.fetchPromises.length).toBe(2);

                    expect(this.fetchFunction.calls.argsFor(1)).toEqual([
                        {start: 1, maxValues: 2, fieldNames: ['CATEGORY'], databases: ALL_INDEXES}
                    ]);
                });

                describe('then the fetch succeeds with 2 values, one of which was returned before, and a total values of 4', function() {
                    beforeEach(respond(1, {
                        totalValues: 4,
                        values: [
                            {value: 'PLANTS', displayValue: 'Plants', count: 10},
                            {value: 'ANIMALS', displayValue: 'Animals', count: 7}
                        ]
                    }));

                    it('has no error and is not empty or loading', checkState());

                    it('adds the correct model to the values collection with count 0, setting the selected flag as appropriate', checkModels([
                        {value: 'PLANTS', displayValue: 'Plants', count: 5, selected: false},
                        {value: 'ANIMALS', displayValue: 'Animals', count: 0, selected: true}
                    ]));

                    it('makes no further requests', function() {
                        expect(this.fetchPromises.length).toBe(2);
                    });

                    describe('then fetchNext is called', function() {
                        beforeEach(fetchNext);

                        it('has no error and is loading', checkState({
                            loading: true
                        }));

                        it('makes a request for page 2', function() {
                            expect(this.fetchPromises.length).toBe(3);

                            expect(this.fetchFunction.calls.argsFor(2)).toEqual([
                                {start: 3, maxValues: 4, fieldNames: ['CATEGORY'], databases: ALL_INDEXES}
                            ]);
                        });
                    });
                });

                describe('then the fetch succeeds with 2 new values and a total values of 3', function() {
                    beforeEach(respond(1, {
                        totalValues: 3,
                        values: [
                            {value: 'FUNGI', displayValue: 'Fungi', count: 9},
                            {value: 'ANIMALS', displayValue: 'Animals', count: 7}
                        ]
                    }));

                    it('adds the correct model to the values collection with count 0, setting the selected flag as appropriate', checkModels([
                        {value: 'PLANTS', displayValue: 'Plants', count: 5, selected: false},
                        {value: 'FUNGI', displayValue: 'Fungi', count: 0, selected: false},
                        {value: 'ANIMALS', displayValue: 'Animals', count: 0, selected: true}
                    ]));

                    it('has no error and is not empty or loading', checkState());

                    describe('then fetchNext is called', function() {
                        beforeEach(fetchNext);

                        it('has no error and is not empty or loading', checkState());

                        it('makes no request because the remaining value has already been fetched', function() {
                            expect(this.fetchPromises.length).toBe(2);
                        });
                    });
                });
            });

            describe('then the fetch succeeds with 2 values and a total values of 10', function() {
                beforeEach(respond(0, {
                    totalValues: 10,
                    values: [
                        {value: 'PLANTS', displayValue: 'Plants', count: 5},
                        {value: 'ANIMALS', displayValue: 'Animals', count: 3}
                    ]
                }));

                it('has no error and is not loading', checkState());

                it('adds new models to the values collection, setting the selected flag as appropriate', checkModels([
                    {value: 'PLANTS', displayValue: 'Plants', count: 5, selected: false},
                    {value: 'ANIMALS', displayValue: 'Animals', count: 3, selected: true}
                ]));

                describe('then a value is selected and another value is deselected', function() {
                    beforeEach(function() {
                        this.paginator.toggleSelection('ANIMALS');
                        this.paginator.toggleSelection('PLANTS');
                    });

                    it('updates the selected values collection', function() {
                        expect(this.selectedValues.length).toBe(2);
                        expect(this.selectedValues.findWhere({field: 'CATEGORY', value: 'PLANTS'})).toBeDefined();
                        expect(this.selectedValues.findWhere({field: 'THINGS', value: 'ANIMALS'})).toBeDefined();
                    });

                    it('updates the models in the values collection', checkModels([
                        {value: 'PLANTS', displayValue: 'Plants', count: 5, selected: true},
                        {value: 'ANIMALS', displayValue: 'Animals', count: 3, selected: false}
                    ]));

                    describe('then fetchNext is called again', function() {
                        beforeEach(fetchNext);

                        it('calls the fetchFunction for page 2', function() {
                            expect(this.fetchPromises.length).toBe(2);

                            expect(this.fetchFunction.calls.argsFor(1)).toEqual([
                                {
                                    start: 3,
                                    maxValues: 4,
                                    fieldNames: ['CATEGORY'],
                                    queryText: 'cat',
                                    databases: ['Generic']
                                }
                            ]);
                        });

                        it('is loading and has no error', checkState({loading: true}));

                        describe('then the fetch succeeds with another 2 values', function() {
                            beforeEach(respond(1, {
                                totalValues: 10,
                                values: [
                                    {count: 1, value: 'INSECTS', displayValue: 'Insects'},
                                    {count: 1, value: 'FUNGI', displayValue: 'Fungi'}
                                ]
                            }));

                            it('has no error and is not loading', checkState());

                            it('adds new models to the values collection, setting the selected flag as appropriate', checkModels([
                                {value: 'PLANTS', displayValue: 'Plants', count: 5, selected: true},
                                {value: 'ANIMALS', displayValue: 'Animals', count: 3, selected: false},
                                {value: 'INSECTS', displayValue: 'Insects', count: 1, selected: false},
                                {value: 'FUNGI', displayValue: 'Fungi', count: 1, selected: false},
                            ]));
                        });
                    });
                });

                describe('then setSelected is called with true', function () {
                    beforeEach(function() {
                        this.paginator.setSelected('PLANTS', true);
                        this.paginator.setSelected('ANIMALS', true); // already true
                    });

                    it('adds to the selectedValues collection', function () {
                        expect(this.selectedValues.length).toBe(3);
                        expect(this.selectedValues.findWhere(
                            { field: 'CATEGORY', value: 'PLANTS' })
                        ).toBeDefined();
                        expect(this.selectedValues.findWhere(
                            { field: 'CATEGORY', value: 'ANIMALS' })
                        ).toBeDefined();
                        expect(this.selectedValues.findWhere(
                            { field: 'THINGS', value: 'ANIMALS' })
                        ).toBeDefined();
                    });

                    it('updates the models in the values collection', checkModels([
                        { value: 'PLANTS', displayValue: 'Plants', count: 5, selected: true },
                        { value: 'ANIMALS', displayValue: 'Animals', count: 3, selected: true }
                    ]));

                });

                describe('then setSelected is called with false', function() {
                    beforeEach(function () {
                        this.paginator.setSelected('PLANTS', false); // already false
                        this.paginator.setSelected('ANIMALS', false);
                    });

                    it('removes from the selectedValues collection', function () {
                        expect(this.selectedValues.length).toBe(1);
                        expect(this.selectedValues.findWhere(
                            { field: 'THINGS', value: 'ANIMALS' })
                        ).toBeDefined();
                    });

                    it('updates the models in the values collection', checkModels([
                        { value: 'PLANTS', displayValue: 'Plants', count: 5, selected: false },
                        { value: 'ANIMALS', displayValue: 'Animals', count: 3, selected: false }
                    ]));

                });

            });

            describe('then the fetch fails', function() {
                beforeEach(function() {
                    this.fetchPromises[0].reject({
                        message: 'These are not the values you are looking for'
                    });
                });

                it('is not loading and has an error', checkState({
                    error: {message: 'These are not the values you are looking for'}
                }));

                describe('then fetchNext is called again', function() {
                    beforeEach(fetchNext);

                    it('is not loading and has an error', checkState({
                        error: {message: 'These are not the values you are looking for'}
                    }));

                    it('does not call the fetchFunction', function() {
                        expect(this.fetchPromises.length).toBe(1);
                    });
                });
            });
        });
    });
});
