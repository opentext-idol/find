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
    'moment',
    'backbone',
    'fieldtext/js/field-text-parser',
    'i18n!find/nls/bundle',
    'js-testing/backbone-mock-factory',
    'find/app/configuration',
    'find/app/model/parametric-collection',
    'find/app/model/date-field-details-model',
    'find/app/model/bucketed-date-collection',
    'find/app/page/search/results/trending/trending-strategy',
    'mock/page/results/trending'
], function(_, $, moment, Backbone, fieldTextParser, i18n, backboneMockFactory, configuration,
            ParametricCollection, ParametricDetailsModel, BucketedParametricCollection,
            trendingStrategy, Trending) {
    'use strict';

    const createFieldText = function (field, value) {
        return new fieldTextParser.ExpressionNode('MATCH', [field], [value]);
    };

    describe('Trending Strategy', function() {
        beforeEach(function() {
            const QueryModel = Backbone.Model.extend({
                getIsoDate: _.constant(null)
            });

            this.queryModel = new QueryModel({
                indexes: ['WIKIPEDIA'],
                autoCorrect: false,
                queryText: 'cat',
                fieldText: createFieldText('fieldA', 'valueA'),
                minScore: 0,
                stateTokens: []
            });

            this.fetchData = [{
                id: 'cheeses',
                values: [{
                    count: 2,
                    displayValue: 'CHEDDAR',
                    value: 'CHEDDAR'
                }, {
                    count: 4,
                    displayValue: 'STILTON',
                    value: 'STILTON'
                }, {
                    count: 2,
                    displayValue: 'BRIE',
                    value: 'BRIE'
                }, {
                    count: 0,
                    displayValue: 'RED LEICESTER',
                    value: 'RED LEICESTER'
                }]
            }];

            this.bucketData = {
                count: 2,
                displayName: 'Display Name',
                max: '2017-05-17T15:51:20Z',
                min: '2017-05-17T15:51:00Z',
                values: [
                    {
                        count: 1,
                        max: '2017-05-17T15:51:20Z',
                        min: '2017-05-17T15:51:15Z',
                        bucketSize: 5
                    }, {
                        count: 1,
                        max: '2017-05-17T15:51:15Z',
                        min: '2017-05-17T15:51:10Z',
                        bucketSize: 5
                    }, {
                        count: 0,
                        max: '2017-05-17T15:51:10Z',
                        min: '2017-05-17T15:51:05Z',
                        bucketSize: 5
                    }, {
                        count: 0,
                        max: '2017-05-17T15:51:05Z',
                        min: '2017-05-17T15:51:00Z',
                        bucketSize: 5
                    }
                ]
            };

            this.rangeFetchData = {
                min: '2017-05-17T15:51:00Z',
                max: '2017-05-17T15:51:20Z'
            };

            this.numberOfValuesToDisplay = 3;
            this.numberOfBuckets = 4;

            this.fetchOptions = {
                queryModel: this.queryModel,
                field: 'cheeses',
                dateField: 'AUTN_DATE',
                numberOfValuesToDisplay: this.numberOfValuesToDisplay,
                currentMax: moment('2017-05-17T15:51:20Z'),
                currentMin: moment('2017-05-17T15:51:01Z'),
                targetNumberOfBuckets: this.numberOfBuckets
            };
        });

        afterEach(function() {
            BucketedParametricCollection.Model.reset();
            ParametricDetailsModel.reset();
            ParametricCollection.reset();
        });

        describe('when fetching the field data', function() {
            beforeEach(function() {
                this.fieldResult = trendingStrategy.fetchField(this.fetchOptions);
            });

            it('should trigger a fetch on the parametric collection', function() {
                expect(ParametricCollection.instances[0].fetch).toHaveBeenCalled();
            });

            it('with the correct arguments', function() {
                expect(ParametricCollection.instances[0].fetch.calls.argsFor(0)[0].data.fieldNames).toEqual(['cheeses']);
                expect(ParametricCollection.instances[0].fetch.calls.argsFor(0)[0].data.queryText).toBe('cat');
                expect(ParametricCollection.instances[0].fetch.calls.argsFor(0)[0].data.fieldText.toString())
                    .toBe('MATCH{valueA}:fieldA');
            });

            describe('when the fetch fails then the method returns a rejected promise', function() {
                beforeEach(function() {
                    ParametricCollection.fetchPromises[0].reject();
                });

                it('returns a rejected promise', function() {
                    expect(this.fieldResult.state()).toBe('rejected');
                });
            });

            describe('when the fetch returns empty', function() {
                beforeEach(function() {
                    ParametricCollection.fetchPromises[0].resolve([]);
                });

                it('has returned a promise that contains the correct information', function() {
                    let fetchResult = null;
                    this.fieldResult.then(function(result) {
                        fetchResult = result;
                    });
                    expect(fetchResult).toEqual([]);
                });
            });

            describe('when the fetch returns an array', function() {
                beforeEach(function() {
                    ParametricCollection.fetchPromises[0].resolve(this.fetchData);
                });

                it('has returned a promise that contains the correct information', function() {
                    let fetchResult = null;
                    this.fieldResult.then(function(result) {
                        fetchResult = result;
                    });
                    expect(fetchResult).toEqual(this.fetchData[0].values);
                });
            });
        });

        describe('when fetching the range data', function() {
            beforeEach(function() {
                this.rangeResult = trendingStrategy.fetchRange(this.fetchData[0].values, this.fetchOptions);
            });

            it('should trigger a fetch on the parametric collection', function() {
                expect(ParametricDetailsModel.instances[0].fetch).toHaveBeenCalled();
            });

            it('with the correct arguments', function() {
                expect(ParametricDetailsModel.instances[0].fetch.calls.argsFor(0)[0].data.fieldName).toEqual('AUTN_DATE');
                expect(ParametricDetailsModel.instances[0].fetch.calls.argsFor(0)[0].data.queryText).toBe('cat');
                expect(ParametricDetailsModel.instances[0].fetch.calls.argsFor(0)[0].data.fieldText.toString())
                    .toBe('MATCH{valueA}:fieldA AND MATCH{CHEDDAR,STILTON,BRIE}:cheeses');
            });

            describe('when the fetch fails then the method returns a rejected promise', function() {
                beforeEach(function() {
                    ParametricDetailsModel.fetchPromises[0].reject();
                });

                it('returns a rejected promise', function() {
                    expect(this.rangeResult.state()).toBe('rejected');
                });
            });

            describe('when the fetch returns an array', function() {
                beforeEach(function() {
                    ParametricDetailsModel.fetchPromises[0].resolve(this.rangeFetchData);
                });

                it('has returned a promise that contains the correct information', function() {
                    let rangeResult = null;
                    this.rangeResult.then(function(result) {
                        rangeResult = result;
                    });
                    expect(rangeResult).toEqual(this.rangeFetchData);
                });
            });
        });

        describe('when fetching the bucketed data', function() {
            beforeEach(function() {
                const bucketedFetchOptions = _.extend({
                    selectedFieldValues: this.fetchData[0].values
                }, this.fetchOptions);

                this.bucketedResult = trendingStrategy.fetchBucketedData(bucketedFetchOptions);
            });

            it('should trigger a fetch for each of the selected values', function() {
                expect(BucketedParametricCollection.Model.fetchPromises).toHaveLength(3);
            });

            it('with the correct arguments', function() {
                const calls = BucketedParametricCollection.Model.instances[0].fetch.calls;
                expect(calls.argsFor(0)[0].data.queryText).toBe('cat');
                expect(calls.argsFor(0)[0].data.fieldText.toString())
                    .toBe('MATCH{valueA}:fieldA AND MATCH{CHEDDAR}:cheeses');
                expect(calls.argsFor(1)[0].data.fieldText.toString())
                    .toBe('MATCH{valueA}:fieldA AND MATCH{STILTON}:cheeses');
                expect(calls.argsFor(2)[0].data.fieldText.toString())
                    .toBe('MATCH{valueA}:fieldA AND MATCH{BRIE}:cheeses');
            });

            describe('one of the fetches fails', function() {
                beforeEach(function() {
                    BucketedParametricCollection.Model.fetchPromises[0].reject();
                });

                it('returns a rejected promise', function() {
                    expect(this.bucketedResult.state()).toBe('rejected');
                });
            });

            describe('the fetches all succeed', function() {
                beforeEach(function() {
                    _.each(BucketedParametricCollection.Model.fetchPromises, function(promise) {
                        promise.resolve(this.bucketData);
                    }, this);
                });

                it('returns the bucketed value data', function() {
                    let bucketedResult = null;
                    this.bucketedResult.then(function() {
                        bucketedResult = Array.prototype.slice.call(arguments);
                    });
                    expect(bucketedResult).toHaveLength(3);
                    expect(bucketedResult[0].values).toEqual(this.bucketData.values)
                });
            });

            describe('when the returned abort method is called', function() {
                beforeEach(function() {
                    this.bucketedResult.abort();
                });

                it('calls abort on the bucketed parametric XHR objects', function() {
                    BucketedParametricCollection.Model.fetchPromises.forEach(function(promise) {
                        expect(promise.abort.calls.count()).toBe(1);
                    });
                });
            });
        });

        describe('when converting bucketed values to chart data', function() {
            beforeEach(function() {
                this.bucketedValues = [{
                    valueName: "CHEDDAR",
                    count: 2,
                    displayName: "Display Name",
                    max: '2017-05-17T15:51:20Z',
                    min: '2017-05-17T15:51:00Z',
                    values: [
                        {count: 1, max: '2017-05-17T15:51:20Z', min: '2017-05-17T15:51:15Z', bucketSize: 5},
                        {count: 1, max: '2017-05-17T15:51:15Z', min: '2017-05-17T15:51:10Z', bucketSize: 5},
                        {count: 0, max: '2017-05-17T15:51:10Z', min: '2017-05-17T15:51:05Z', bucketSize: 5},
                        {count: 0, max: '2017-05-17T15:51:05Z', min: '2017-05-17T15:51:00Z', bucketSize: 5}]
                }, {
                    valueName: "STILTON",
                    count: 2,
                    displayName: "Display Name",
                    max: '2017-05-17T15:51:20Z',
                    min: '2017-05-17T15:51:00Z',
                    values: [
                        {count: 1, max: '2017-05-17T15:51:20Z', min: '2017-05-17T15:51:15Z', bucketSize: 5},
                        {count: 1, max: '2017-05-17T15:51:15Z', min: '2017-05-17T15:51:10Z', bucketSize: 5},
                        {count: 0, max: '2017-05-17T15:51:10Z', min: '2017-05-17T15:51:05Z', bucketSize: 5},
                        {count: 0, max: '2017-05-17T15:51:05Z', min: '2017-05-17T15:51:00Z', bucketSize: 5}
                    ]
                }, {
                    valueName: "BRIE",
                    count: 2,
                    displayName: "Display Name",
                    max: '2017-05-17T15:51:20Z',
                    min: '2017-05-17T15:51:00Z',
                    values: [
                        {count: 1, max: '2017-05-17T15:51:20Z', min: '2017-05-17T15:51:15Z', bucketSize: 5},
                        {count: 1, max: '2017-05-17T15:51:15Z', min: '2017-05-17T15:51:10Z', bucketSize: 5},
                        {count: 0, max: '2017-05-17T15:51:10Z', min: '2017-05-17T15:51:05Z', bucketSize: 5},
                        {count: 0, max: '2017-05-17T15:51:05Z', min: '2017-05-17T15:51:00Z', bucketSize: 5}
                    ]
                }, {
                    valueName: "RED LEICESTER",
                    count: 2,
                    displayName: "Display Name",
                    max: '2017-05-17T15:51:20Z',
                    min: '2017-05-17T15:51:00Z',
                    values: [
                        {count: 1, max: '2017-05-17T15:51:20Z', min: '2017-05-17T15:51:15Z', bucketSize: 5},
                        {count: 1, max: '2017-05-17T15:51:15Z', min: '2017-05-17T15:51:10Z', bucketSize: 5},
                        {count: 0, max: '2017-05-17T15:51:10Z', min: '2017-05-17T15:51:05Z', bucketSize: 5},
                        {count: 0, max: '2017-05-17T15:51:05Z', min: '2017-05-17T15:51:00Z', bucketSize: 5}
                    ]
                }];

                this.chartData = trendingStrategy.createChartData({
                    bucketedValues: this.bucketedValues.map(function(data) {
                        return _.extend(data, {
                            min: moment(data.min),
                            max: moment(data.max),
                            values: data.values.map(function(value) {
                                return _.extend(value, {min: moment(value.min), max: moment(value.max)});
                            })
                        });
                    }),
                    currentMax: moment('2017-05-17T15:51:20Z'),
                    currentMin: moment('2017-05-17T15:51:01Z')
                });
            });

            it('should return only the values that fit in the current min-max range', function() {
                expect(this.chartData.data).toHaveLength(4);
                expect(this.chartData.data[0].points).toHaveLength(4);
            });

            it('should return data with mid points', function() {
                expect(this.chartData.data[0].points[2].mid).toBeTruthy();
                expect(this.chartData.data[1].points[2].mid).toBeTruthy();
                expect(this.chartData.data[2].points[2].mid).toBeTruthy();
                expect(this.chartData.data[3].points[2].mid).toBeTruthy();
            });

            it('returns the y axis unit', function() {
                expect(this.chartData.yUnit).toBe('MINUTE');
            });

            it('converts the counts to rates', function() {
                expect(this.chartData.data[0].points[0].rate).toBe(12);
            });

            it('returns out the minRate and maxRate', function() {
                expect(this.chartData.minRate).toBe(0);
                expect(this.chartData.maxRate).toBe(12);
            });
        });
    });
});
