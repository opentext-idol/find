/*
 *  Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 *  Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone',
    'find/app/page/search/results/trending/trending'
], function(_, Backbone, Trending) {
    'use strict';

    const testData = [
        [["2008-07-01T14:39:39.200Z", 36], ["2009-06-19T06:19:18.400Z", 9], ["2010-06-06T21:58:57.600Z", 241], ["2011-05-25T13:38:36.800Z", 90], ["2012-05-12T05:18:16.000Z", 123]],
        [["2008-07-01T14:39:39.200Z", 0], ["2009-06-19T06:19:18.400Z", 0], ["2010-06-06T21:58:57.600Z", 117], ["2011-05-25T13:38:36.800Z", 0], ["2012-05-12T05:18:16.000Z", 0]],
        [["2008-07-01T14:39:39.200Z", 64], ["2009-06-19T06:19:18.400Z", 83], ["2010-06-06T21:58:57.600Z", 64], ["2011-05-25T13:38:36.800Z", 0], ["2012-05-12T05:18:16.000Z", 0]],
        [["2008-07-01T14:39:39.200Z", 3], ["2009-06-19T06:19:18.400Z", 38], ["2010-06-06T21:58:57.600Z", 53], ["2011-05-25T13:38:36.800Z", 43], ["2012-05-12T05:18:16.000Z", 2]],
        [["2008-07-01T14:39:39.200Z", 0], ["2009-06-19T06:19:18.400Z", 0], ["2010-06-06T21:58:57.600Z", 9], ["2011-05-25T13:38:36.800Z", 0], ["2012-05-12T05:18:16.000Z", 0]]
    ];
    const testNames = ['Dragon', 'Phoenix', 'Griffin', 'Hydra', 'Siren'];
    const chartWidth = 600;
    const chartHeight = 400;
    const newChartWidth = 1000;
    const newChartHeight = 780;

    function toDateObject(dateString) {
        return new Date(dateString);
    };

    describe('Trending', function() {
        it('exposes a constructor function', function() {
            expect(typeof Trending).toBe('function');
        });

        beforeEach(function() {
            _.each(testData, function (value) {
                _.each(value, function (point) {
                    point[0] = toDateObject(point[0]);
                });
            });
            this.view = new Backbone.View();
            this.trending = Trending({
                getContainerCallback: function() {
                    return this.view.$el.get(0);
                }.bind(this)
            });
        });

        it('has a draw function and a resize function', function() {
            expect(typeof this.trending.draw).toBe('function');
        });

        describe('after the chart is drawn', function() {
            beforeEach(function() {
                this.trending.draw({
                    data: testData,
                    names: testNames,
                    maxDate: toDateObject('2012-05-12T05:18:16.000Z'),
                    minDate: toDateObject('2008-07-01T14:39:39.200Z'),
                    containerWidth: chartWidth,
                    containerHeight: chartHeight,
                });
            });

            it('has rendered an svg for the chart', function() {
                expect(this.view.$('svg').length).toBe(1);
                expect(parseInt(this.view.$('svg').get(0).getAttribute('width'))).toBe(chartWidth);
                expect(parseInt(this.view.$('svg').get(0).getAttribute('height'))).toBe(chartHeight);
            });

            it('has rendered a line with 5 points for each value', function() {
                expect(this.view.$('.line').length).toBe(5);
                expect(this.view.$('circle').length).toBe(25);
            });

            it('has rendered a legend with the correct labels', function() {
                const labels = this.view.$('.legend-text');
                expect(labels.length).toBe(5);
                _.each(labels, function(label) {
                    expect(_.contains(testNames, label.textContent)).toBe(true);
                });
            });

            it('has rendered x and y axes', function() {
                expect(this.view.$('.x-axis').length).toBe(1);
                expect(this.view.$('.y-axis').length).toBe(1);
            });

            describe('on redrawing', function() {
                beforeEach(function() {
                    this.view.$el.empty();
                    this.trending.draw({
                        data: testData,
                        names: testNames,
                        maxDate: toDateObject('2012-05-12T05:18:16.000Z'),
                        minDate: toDateObject('2008-07-01T14:39:39.200Z'),
                        containerWidth: newChartWidth,
                        containerHeight: newChartHeight,
                    });
                });

                it('the chart is rendered with the new dimensions', function() {
                    expect(this.view.$('svg').length).toBe(1);
                    expect(parseInt(this.view.$('svg').get(0).getAttribute('width'))).toBe(newChartWidth);
                    expect(parseInt(this.view.$('svg').get(0).getAttribute('height'))).toBe(newChartHeight);
                });
            });
        });
    });
});