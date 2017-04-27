/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define(function() {
    'use strict';

    const fetchFieldSpy = jasmine.createSpy('fetchField');
    const fetchRangeSpy = jasmine.createSpy('fetchRange');
    const fetchBucketedDataSpy = jasmine.createSpy('fetchBucketedData');
    const createChartDataSpy = jasmine.createSpy('createChartData');

    const MockTrendingStrategy = {
        fetchField: fetchFieldSpy,
        fetchRange: fetchRangeSpy,
        fetchBucketedData: fetchBucketedDataSpy,
        createChartData: createChartDataSpy
    };

    fetchFieldSpy.and.callFake(function() {
        const promise = $.Deferred();

        MockTrendingStrategy.fetchFieldPromises.push(promise);
        return promise;
    });

    fetchRangeSpy.and.callFake(function() {
        const promise = $.Deferred();

        MockTrendingStrategy.fetchRangeDataPromises.push(promise);
        return promise;
    });

    fetchBucketedDataSpy.and.callFake(function() {
        const promise = $.Deferred();

        MockTrendingStrategy.fetchBucketedDataPromises.push(promise);
        return promise;
    });

    createChartDataSpy.and.callFake(function() {
        return {
            data: [{
                points: [
                    {count: 1, mid: 0, min: 5, max: 10}
                ]
            }, {
                points: [
                    {count: 3, mid: 10, min: 15, max: 20}
                ]
            }]
        };
    });

    MockTrendingStrategy.reset = function() {
        fetchFieldSpy.calls.reset();
        fetchRangeSpy.calls.reset();
        fetchBucketedDataSpy.calls.reset();
        createChartDataSpy.calls.reset();
        MockTrendingStrategy.fetchFieldPromises = [];
        MockTrendingStrategy.fetchRangeDataPromises = [];
        MockTrendingStrategy.fetchBucketedDataPromises = [];
    };

    MockTrendingStrategy.reset();
    return MockTrendingStrategy;
});