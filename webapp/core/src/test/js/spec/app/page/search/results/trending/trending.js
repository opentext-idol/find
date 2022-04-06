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
    'backbone',
    'i18n!find/nls/bundle',
    'find/app/page/search/results/trending/trending'
], function(_, Backbone, i18n, Trending) {
    'use strict';

    const chartData = {
        minRate: 0,
        maxRate: 241 / 1142.6,
        yUnit: 'HOUR',
        data: [
            {
                name: 'Dragon',
                points: [
                    {
                        count: 36,
                        mid: new Date('2009-08-12T18:18:00.000Z'),
                        min: new Date('2009-07-19T23:00:00.000Z'),
                        max: new Date('2009-09-05T13:36:00.000Z')
                    },
                    {
                        count: 9,
                        mid: new Date('2009-09-29T08:54:00.000Z'),
                        min: new Date('2009-09-05T13:36:00.000Z'),
                        max: new Date('2009-10-23T04:12:00.000Z')
                    },
                    {
                        count: 241,
                        mid: new Date('2009-11-15T23:30:00.000Z'),
                        min: new Date('2009-10-23T04:12:00.000Z'),
                        max: new Date('2009-12-09T18:48:00.000Z')
                    },
                    {
                        count: 90,
                        mid: new Date('2010-01-02T14:06:00.000Z'),
                        min: new Date('2009-12-09T18:48:00.000Z'),
                        max: new Date('2010-01-26T09:24:00.000Z')
                    },
                    {
                        count: 123,
                        mid: new Date('2010-02-19T04:42:00.000Z'),
                        min: new Date('2010-01-26T09:24:00.000Z'),
                        max: new Date('2010-03-15T00:00:00.000Z')
                    }
                ]
            },
            {
                name: 'Phoenix',
                points: [
                    {
                        count: 0,
                        mid: new Date('2009-08-12T18:18:00.000Z'),
                        min: new Date('2009-07-19T23:00:00.000Z'),
                        max: new Date('2009-09-05T13:36:00.000Z')
                    },
                    {
                        count: 0,
                        mid: new Date('2009-09-29T08:54:00.000Z'),
                        min: new Date('2009-09-05T13:36:00.000Z'),
                        max: new Date('2009-10-23T04:12:00.000Z')
                    },
                    {
                        count: 117,
                        mid: new Date('2009-11-15T23:30:00.000Z'),
                        min: new Date('2009-10-23T04:12:00.000Z'),
                        max: new Date('2009-12-09T18:48:00.000Z')
                    },
                    {
                        count: 0,
                        mid: new Date('2010-01-02T14:06:00.000Z'),
                        min: new Date('2009-12-09T18:48:00.000Z'),
                        max: new Date('2010-01-26T09:24:00.000Z')
                    },
                    {
                        count: 0,
                        mid: new Date('2010-02-19T04:42:00.000Z'),
                        min: new Date('2010-01-26T09:24:00.000Z'),
                        max: new Date('2010-03-15T00:00:00.000Z')
                    }
                ]
            },
            {
                name: 'Griffin',
                points: [
                    {
                        count: 64,
                        mid: new Date('2009-08-12T18:18:00.000Z'),
                        min: new Date('2009-07-19T23:00:00.000Z'),
                        max: new Date('2009-09-05T13:36:00.000Z')
                    },
                    {
                        count: 83,
                        mid: new Date('2009-09-29T08:54:00.000Z'),
                        min: new Date('2009-09-05T13:36:00.000Z'),
                        max: new Date('2009-10-23T04:12:00.000Z')
                    },
                    {
                        count: 64,
                        mid: new Date('2009-11-15T23:30:00.000Z'),
                        min: new Date('2009-10-23T04:12:00.000Z'),
                        max: new Date('2009-12-09T18:48:00.000Z')
                    },
                    {
                        count: 0,
                        mid: new Date('2010-01-02T14:06:00.000Z'),
                        min: new Date('2009-12-09T18:48:00.000Z'),
                        max: new Date('2010-01-26T09:24:00.000Z')
                    },
                    {
                        count: 0,
                        mid: new Date('2010-02-19T04:42:00.000Z'),
                        min: new Date('2010-01-26T09:24:00.000Z'),
                        max: new Date('2010-03-15T00:00:00.000Z')
                    }
                ]
            },
            {
                name: 'Hydra',
                points: [
                    {
                        count: 3,
                        mid: new Date('2009-08-12T18:18:00.000Z'),
                        min: new Date('2009-07-19T23:00:00.000Z'),
                        max: new Date('2009-09-05T13:36:00.000Z')
                    },
                    {
                        count: 38,
                        mid: new Date('2009-09-29T08:54:00.000Z'),
                        min: new Date('2009-09-05T13:36:00.000Z'),
                        max: new Date('2009-10-23T04:12:00.000Z')
                    },
                    {
                        count: 53,
                        mid: new Date('2009-11-15T23:30:00.000Z'),
                        min: new Date('2009-10-23T04:12:00.000Z'),
                        max: new Date('2009-12-09T18:48:00.000Z')
                    },
                    {
                        count: 43,
                        mid: new Date('2010-01-02T14:06:00.000Z'),
                        min: new Date('2009-12-09T18:48:00.000Z'),
                        max: new Date('2010-01-26T09:24:00.000Z')
                    },
                    {
                        count: 2,
                        mid: new Date('2010-02-19T04:42:00.000Z'),
                        min: new Date('2010-01-26T09:24:00.000Z'),
                        max: new Date('2010-03-15T00:00:00.000Z')
                    }
                ]
            },
            {
                name: 'Siren',
                points: [
                    {
                        count: 0,
                        mid: new Date('2009-08-12T18:18:00.000Z'),
                        min: new Date('2009-07-19T23:00:00.000Z'),
                        max: new Date('2009-09-05T13:36:00.000Z')
                    },
                    {
                        count: 0,
                        mid: new Date('2009-09-29T08:54:00.000Z'),
                        min: new Date('2009-09-05T13:36:00.000Z'),
                        max: new Date('2009-10-23T04:12:00.000Z')
                    },
                    {
                        count: 9,
                        mid: new Date('2009-11-15T23:30:00.000Z'),
                        min: new Date('2009-10-23T04:12:00.000Z'),
                        max: new Date('2009-12-09T18:48:00.000Z')
                    },
                    {
                        count: 0,
                        mid: new Date('2010-01-02T14:06:00.000Z'),
                        min: new Date('2009-12-09T18:48:00.000Z'),
                        max: new Date('2010-01-26T09:24:00.000Z')
                    },
                    {
                        count: 0,
                        mid: new Date('2010-02-19T04:42:00.000Z'),
                        min: new Date('2010-01-26T09:24:00.000Z'),
                        max: new Date('2010-03-15T00:00:00.000Z')
                    }
                ]
            }
        ]
    };

    const testNames = ['Dragon', 'Phoenix', 'Griffin', 'Hydra', 'Siren'];
    const chartWidth = 600;
    const chartHeight = 400;
    const newChartWidth = 1000;
    const newChartHeight = 780;

    describe('Trending', function() {
        it('exposes a constructor function', function() {
            expect(typeof Trending).toBe('function');
        });

        describe('after the chart is drawn', function() {
            beforeEach(function() {
                this.view = new Backbone.View();
                this.view.$el.height(chartHeight);
                this.view.$el.width(chartWidth);

                this.trending = new Trending({
                    el: this.view.$el.get(0),
                    tooltipText: _.constant('Tooltip text'),
                    yAxisUnitsText: _.constant('Heure'),
                    yAxisLabelForUnit: _.constant('Docs per hour')
                });

                this.trending.draw({
                    reloaded: true,
                    chartData: chartData,
                    maxDate: new Date('2012-05-12T05:18:16.000Z'),
                    minDate: new Date('2008-07-01T14:39:39.200Z')
                });
            });

            it('has rendered an svg for the chart', function() {
                expect(this.view.$('svg')).toHaveLength(1);
                expect(parseInt(this.view.$('svg').get(0).getAttribute('width'))).toEqual(chartWidth);
                expect(parseInt(this.view.$('svg').get(0).getAttribute('height'))).toEqual(chartHeight);
            });

            it('has rendered a line with 5 points for each value', function() {
                expect(this.view.$('.line')).toHaveLength(5);
                expect(this.view.$('circle')).toHaveLength(25);
            });

            it('has rendered a legend with the correct labels', function() {
                const labels = this.view.$('.legend-text');
                expect(labels).toHaveLength(5);

                _.each(labels, function(label) {
                    expect(_.contains(testNames, label.textContent)).toBe(true);
                });
            });

            it('has rendered x and y axes', function() {
                expect(this.view.$('.x-axis')).toHaveLength(1);
                expect(this.view.$('.y-axis')).toHaveLength(1);
            });

            describe('on redrawing', function() {
                beforeEach(function() {
                    this.view.$el.height(newChartHeight);
                    this.view.$el.width(newChartWidth);

                    this.trending.draw({
                        reloaded: true,
                        el: this.view.$el.get(0),
                        chartData: chartData,
                        maxDate: new Date('2012-05-12T05:18:16.000Z'),
                        minDate: new Date('2008-07-01T14:39:39.200Z'),
                        tooltipText: i18n['search.resultsView.trending.tooltipText']
                    });
                });

                it('the chart is rendered with the new dimensions', function() {
                    expect(this.view.$('svg')).toHaveLength(1);
                    expect(parseInt(this.view.$('svg').get(0).getAttribute('width'))).toBe(newChartWidth);
                    expect(parseInt(this.view.$('svg').get(0).getAttribute('height'))).toBe(newChartHeight);
                });
            });
        });
    });
});
