define([
    'underscore',
    'jquery',
    'backbone',
    'i18n!find/nls/bundle',
    'js-testing/backbone-mock-factory',
    'find/app/configuration',
    'find/app/model/parametric-collection',
    'find/app/model/parametric-field-details-model',
    'find/app/model/bucketed-parametric-collection',
    'find/app/page/search/results/trending/trending-strategy',
    'mock/page/results/trending'
], function (_, $, Backbone, i18n, backboneMockFactory, configuration, ParametricCollection, ParametricDetailsModel,
             BucketedParametricCollection, trendingStrategy, Trending) {
    'use strict';

    describe('Trending Strategy', function() {
        beforeEach(function() {
            const QueryModel = Backbone.Model.extend({
                getIsoDate: _.constant(null)
            });

            this.queryModel = new QueryModel({
                indexes: ['WIKIPEDIA'],
                autoCorrect: false,
                queryText: 'cat',
                fieldText: null,
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
                max: 20,
                min: 0,
                values: [
                    {
                        count: 1,
                        max: 20,
                        min: 15
                    }, {
                        count: 1,
                        max: 15,
                        min: 10
                    }, {
                        count: 0,
                        max: 10,
                        min: 5
                    }, {
                        count: 0,
                        max: 5,
                        min: 0
                    }
                ]
            };

            this.rangeFetchData = {
                min: 0,
                max: 20
            };

            this.numberOfValuesToDisplay = 5;
            this.numberOfBuckets = 4;

            this.fetchOptions = {
                queryModel: this.queryModel,
                selectedParametricValues: new Backbone.Collection(),
                field: 'cheeses',
                dateField: 'AUTN_DATE',
                numberOfValuesToDisplay: this.numberOfValuesToDisplay,
                currentMax: 1,
                currentMin: 10,
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
                expect(ParametricCollection.instances[0].fetch.calls.argsFor(0)[0].data.queryText).toBe(this.queryModel.get('queryText'));
            });

            describe('when the fetch fails then the method returns a rejected promise', function () {
                beforeEach(function () {
                    ParametricCollection.fetchPromises[0].reject();
                });

                it('returns a rejected promise', function() {
                    expect(this.fieldResult.state()).toBe('rejected');
                });
            });

            describe('when the fetch returns empty', function() {
                beforeEach(function () {
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
                beforeEach(function () {
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
                expect(ParametricDetailsModel.instances[0].fetch.calls.argsFor(0)[0].data.queryText).toBe(this.queryModel.get('queryText'));
            });

            describe('when the fetch fails then the method returns a rejected promise', function () {
                beforeEach(function () {
                    ParametricDetailsModel.fetchPromises[0].reject();
                });

                it('returns a rejected promise', function() {
                    expect(this.rangeResult.state()).toBe('rejected');
                });
            });

            describe('when the fetch returns an array', function() {
                beforeEach(function () {
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
                const bucketedFetchOptions = _.extend(this.fetchOptions, { selectedFieldValues: this.fetchData[0].values });
                this.bucketedResult = trendingStrategy.fetchBucketedData(bucketedFetchOptions);
            });

            it('should trigger a fetch for each of the selected values', function () {
                expect(BucketedParametricCollection.Model.fetchPromises).toHaveLength(4);
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
                    _.each(BucketedParametricCollection.Model.fetchPromises, function (promise) {
                        promise.resolve(this.bucketData);
                    }, this);
                });

                it('returns the bucketed value data', function() {
                    let bucketedResult = null;
                    this.bucketedResult.then(function() {
                        bucketedResult = Array.prototype.slice.call(arguments);
                    });
                    expect(bucketedResult).toHaveLength(4);
                    expect(bucketedResult[0].values).toEqual(this.bucketData.values)
                });
            });
        });

        describe('when converting bucketed values to chart data', function() {
            beforeEach(function(){
                this.bucketedValues = [{
                    "valueName": "CHEDDAR",
                    "count": 2,
                    "displayName": "Display Name",
                    "max": 20,
                    "min": 0,
                    "values": [
                        {"count": 1, "max": 20, "min": 15},
                        {"count": 1, "max": 15, "min": 10},
                        {"count": 0, "max": 10, "min": 5},
                        {"count": 0, "max": 5, "min": 0}]
                }, {
                    "valueName": "STILTON",
                    "count": 2,
                    "displayName": "Display Name",
                    "max": 20,
                    "min": 0,
                    "values": [
                        {"count": 1, "max": 20, "min": 15},
                        {"count": 1, "max": 15, "min": 10},
                        {"count": 0, "max": 10, "min": 5},
                        {"count": 0, "max": 5, "min": 0}
                    ]
                }, {
                    "valueName": "BRIE",
                    "count": 2,
                    "displayName": "Display Name",
                    "max": 20,
                    "min": 0,
                    "values": [
                        {"count": 1, "max": 20, "min": 15},
                        {"count": 1, "max": 15, "min": 10},
                        {"count": 0, "max": 10, "min": 5},
                        {"count": 0, "max": 5, "min": 0}
                    ]
                }, {
                    "valueName": "RED LEICESTER",
                    "count": 2,
                    "displayName": "Display Name",
                    "max": 20,
                    "min": 0,
                    "values": [
                        {"count": 1, "max": 20, "min": 15},
                        {"count": 1, "max": 15, "min": 10},
                        {"count": 0, "max": 10, "min": 5},
                        {"count": 0, "max": 5, "min": 0}
                    ]
                }];

                this.chartData = trendingStrategy.createChartData({
                    bucketedValues: this.bucketedValues,
                    currentMax: 20,
                    currentMin: 1
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