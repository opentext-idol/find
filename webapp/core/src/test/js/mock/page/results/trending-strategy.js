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
    'jquery'
], function($) {
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
        promise.abort = jasmine.createSpy('abort');
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
