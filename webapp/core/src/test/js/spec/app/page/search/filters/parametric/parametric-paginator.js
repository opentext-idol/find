/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'backbone',
    'find/app/page/search/filters/parametric/parametric-paginator'
], function($, Backbone, ParametricPaginator) {
    'use strict';

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

    describe('ParametricPaginator', function(){
        beforeEach(function() {
            this.fetchPromises = [];
            this.fetchFunction = jasmine.createSpy('fetchFunction');

            this.selectedValues = new Backbone.Collection([
                {field: 'CATEGORY', value: 'ANIMALS'},
                {field: 'THINGS', value: 'ANIMALS'}
            ]);

            this.fetchFunction.and.callFake(function() {
                const promise = $.Deferred();
                this.fetchPromises.push(promise);
                return promise;
            }.bind(this));

            this.paginator = new ParametricPaginator({
                fetchData: {queryText: 'cat'},
                fetchFunction: this.fetchFunction,
                fieldName: 'CATEGORY',
                pageSize: 2,
                selectedValues: this.selectedValues
            });
        });

        it('constructs with no error and not loading or empty', checkState());

        it('does not fetch on construction', function() {
            expect(this.fetchPromises.length).toBe(0);
        });

        it('constructs with an empty values collection', function() {
            expect(this.paginator.valuesCollection.length).toBe(0);
        });

        describe('when fetchNext is called', function() {
            beforeEach(function() {
                this.paginator.fetchNext();
            });

            it('calls the fetchFunction for page 1', function() {
                expect(this.fetchPromises.length).toBe(1);

                expect(this.fetchFunction.calls.argsFor(0)).toEqual([
                    {start: 1, maxValues: 2, fieldNames: ['CATEGORY'], queryText: 'cat'}
                ]);
            });

            it('is loading and has no error', checkState({loading: true}));

            describe('when fetchNext is called before the request succeeds', function() {
                beforeEach(function() {
                    this.paginator.fetchNext();
                });

                it('does not call the fetchFunction again', function() {
                    expect(this.fetchPromises.length).toBe(1);
                });

                it('is still loading and has no error', checkState({loading: true}));
            });

            describe('then the fetch succeeds with no values', function() {
                beforeEach(function() {
                    this.fetchPromises[0].resolve({
                        totalValues: 0,
                        values: []
                    });
                });

                it('has no error and is not loading but is empty', checkState({
                    empty: true
                }));

                describe('then fetchNext is called again', function() {
                    beforeEach(function() {
                        this.paginator.fetchNext();
                    });

                    it('has no error and is not loading because there are no more parametric values', checkState({
                        empty: true
                    }));

                    it('does not call the fetchFunction', function() {
                        expect(this.fetchPromises.length).toBe(1);
                    });
                });
            });

            describe('then the fetch succeeds with 2 values and a total values of 2', function() {
                beforeEach(function() {
                    this.fetchPromises[0].resolve({
                        totalValues: 2,
                        values: [
                            {value: 'PLANTS', count: 5},
                            {value: 'ANIMALS', count: 3}
                        ]
                    });
                });

                it('has no error and is not loading', checkState());

                describe('then fetchNext is called again', function() {
                    beforeEach(function() {
                        this.paginator.fetchNext();
                    });

                    it('has no error and is not loading because there are no more parametric values', checkState());

                    it('does not call the fetchFunction', function() {
                        expect(this.fetchPromises.length).toBe(1);
                    });
                });
            });

            describe('then the fetch succeeds with 2 values and a total values of 10', function() {
                beforeEach(function() {
                    this.fetchPromises[0].resolve({
                        totalValues: 10,
                        values: [
                            {value: 'PLANTS', count: 5},
                            {value: 'ANIMALS', count: 3}
                        ]
                    });
                });

                it('has no error and is not loading', checkState());

                it('adds new models to the values collection, setting the selected flag as appropriate', checkModels([
                    {value: 'PLANTS', count: 5, selected: false},
                    {value: 'ANIMALS', count: 3, selected: true}
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
                        {value: 'PLANTS', count: 5, selected: true},
                        {value: 'ANIMALS', count: 3, selected: false}
                    ]));

                    describe('then fetchNext is called again', function() {
                        beforeEach(function() {
                            this.paginator.fetchNext();
                        });

                        it('calls the fetchFunction for page 1', function() {
                            expect(this.fetchPromises.length).toBe(2);

                            expect(this.fetchFunction.calls.argsFor(1)).toEqual([
                                {start: 3, maxValues: 4, fieldNames: ['CATEGORY'], queryText: 'cat'}
                            ]);
                        });

                        it('is loading and has no error', checkState({loading: true}));

                        describe('then the fetch succeeds with another 2 values', function() {
                            beforeEach(function() {
                                this.fetchPromises[1].resolve({
                                    totalValues: 10,
                                    values: [
                                        {count: 1, value: 'INSECTS'},
                                        {count: 1, value: 'FUNGI'}
                                    ]
                                });
                            });

                            it('has no error and is not loading', checkState());

                            it('adds new models to the values collection, setting the selected flag as appropriate', checkModels([
                                {value: 'PLANTS', count: 5, selected: true},
                                {value: 'ANIMALS', count: 3, selected: false},
                                {value: 'INSECTS', count: 1, selected: false},
                                {value: 'FUNGI', count: 1, selected: false},
                            ]));
                        });
                    });
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
                    beforeEach(function() {
                        this.paginator.fetchNext()
                    });

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
