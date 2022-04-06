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
    'i18n!find/nls/bundle',
    'js-testing/backbone-mock-factory',
    'find/app/configuration',
    'find/app/model/parametric-collection',
    'find/app/model/date-field-details-model',
    'find/app/page/search/results/trending/trending-view',
    'mock/page/results/trending-strategy',
    'mock/page/results/trending'
], function(_, $, Backbone, i18n, backboneMockFactory, configuration, ParametricCollection,
            ParametricDetailsModel, TrendingView, TrendingStrategy, Trending) {
    'use strict';

    const originalDebounce = _.debounce;

    const xhr = {
        status: 500,
        responseJSON: {
            message: 'error',
            uuid: '1234'
        }
    };

    const DEFAULT_NUMBER_OF_BUCKETS = 20;
    const MIN_NUMBER_OF_BUCKETS = 3;
    const MAX_NUMBER_OF_BUCKETS = 100;

    describe('Trending view', function() {
        beforeEach(function() {
            _.debounce = function(callback) {
                return function() {
                    callback.apply(this, arguments);
                };
            };

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

            this.parametricCollection = new Backbone.Collection([
                {
                    id: 'cheeses',
                    totalValues: 200
                },
                {
                    id: 'breads',
                    totalValues: 450
                },
                {
                    id: 'meats',
                    totalValues: 140
                },
                {
                    id: 'vegetables',
                    totalValues: 223
                }
            ]);

            this.parametricFieldsCollection = new Backbone.Collection([
                {
                    id: 'cheeses',
                    displayName: 'Cheeses',
                    totalValues: 1000,
                    type: 'Parametric'
                },
                {
                    id: 'breads',
                    displayName: 'Breads',
                    totalValues: 1000,
                    type: 'Parametric'
                },
                {
                    id: 'meats',
                    displayName: 'Meats',
                    totalValues: 1000,
                    type: 'Parametric'
                },
                {
                    id: 'vegetables',
                    displayName: 'Veg',
                    totalValues: 1000,
                    type: 'Parametric'
                },
                {
                    id: 'numeric to be discarded',
                    displayName: '',
                    totalValues: 38,
                    type: 'Numeric'
                }
            ]);

            configuration.and.returnValue({
                trending: {
                    dateField: "AUTN_DATE",
                    defaultNumberOfBuckets: DEFAULT_NUMBER_OF_BUCKETS,
                    minNumberOfBuckets: MIN_NUMBER_OF_BUCKETS,
                    maxNumberOfBuckets: MAX_NUMBER_OF_BUCKETS,
                    numberOfValues: 10
                }
            });

            this.view = new TrendingView({
                queryModel: this.queryModel,
                parametricCollection: this.parametricCollection,
                parametricFieldsCollection: this.parametricFieldsCollection
            });

            $(document.body).append(this.view.$el);
            this.view.render();
        });

        afterEach(function() {
            this.view.remove();
            _.debounce = originalDebounce;
            ParametricCollection.reset();
            ParametricDetailsModel.reset();
            TrendingStrategy.reset();
            Trending.reset();
        });

        it('should render the template html', function() {
            expect(this.view.$('.trending-chart')).toHaveLength(1);
        });

        describe('and when the parametric collection syncs', function() {
            beforeEach(function() {
                this.parametricCollection.trigger('sync');
            });

            it('should render and populate the field selector', function() {
                const $option = this.view.$('.trending-field-selector option');
                expect($option).toHaveLength(4);
                expect($option[0].text).toBe('Cheeses (200)');
                expect($option[1].text).toBe('Breads (450)');
                expect($option[2].text).toBe('Meats (140)');
                expect($option[3].text).toBe('Veg (223)');
            });

            it('should call fetch field on the trending strategy', function() {
                expect(TrendingStrategy.fetchFieldPromises).toHaveLength(1);
            });

            describe('and the fetch fails', function() {
                beforeEach(function() {
                    TrendingStrategy.fetchFieldPromises[0].reject(xhr);
                });

                it('should display an error message', function() {
                    expect(this.view.$('.trending-error')).not.toHaveClass('hide');
                });

                it('should hide the snap to now button', function() {
                    expect(this.view.$('.trending-snap-to-now')).toHaveClass('hide');
                });

                it('should hide the chart', function() {
                    expect(this.view.$('.trending-chart')).toHaveClass('hide');
                });
            });

            describe('and the fetch succeeds', function() {
                beforeEach(function() {
                    TrendingStrategy.fetchFieldPromises[0].resolve([
                        {
                            count: 2,
                            displayValue: 'CHEDDAR',
                            value: 'CHEDDAR'
                        },
                        {
                            count: 4,
                            displayValue: 'STILTON',
                            value: 'STILTON'
                        },
                        {
                            count: 2,
                            displayValue: 'BRIE',
                            value: 'BRIE'
                        },
                        {
                            count: 0,
                            displayValue: 'RED LEICESTER',
                            value: 'RED LEICESTER'
                        }
                    ]);
                });

                it('should fetch range data', function() {
                    expect(TrendingStrategy.fetchRangeDataPromises).toHaveLength(1);
                });

                describe('and the fetch for range details fails', function() {
                    beforeEach(function() {
                        TrendingStrategy.fetchRangeDataPromises[0].reject(xhr);
                    });

                    it('should display an error message', function() {
                        expect(this.view.$('.trending-error')).not.toHaveClass('hide');
                    });

                    it('should hide the snap to now button', function() {
                        expect(this.view.$('.trending-snap-to-now')).toHaveClass('hide');
                    });

                    it('should hide the chart', function() {
                        expect(this.view.$('.trending-chart')).toHaveClass('hide');
                    });

                    it('should hide the slider', function() {
                        expect(this.view.$('.trending-slider')).toHaveClass('hide');
                    });
                });

                describe('and the fetch for range details succeeds', function() {
                    beforeEach(function() {
                        TrendingStrategy.fetchRangeDataPromises[0].resolve({
                            min: 0,
                            max: 20
                        });
                    });

                    it('should call the trending strategy fetch buckets method', function() {
                        // In PhantomJS, fetchBucketedData() may be called multiple times because
                        // of how we mock out _.debounce()
                        expect(TrendingStrategy.fetchBucketedDataPromises.length).toBeGreaterThan(0);
                    });

                    describe('then a different field is selected before the range details have returned', function() {
                        beforeEach(function() {
                            this.view.$('.parametric-select')
                                .val('breads')
                                .trigger('change');
                        });

                        it('aborts the previous field requests', function() {
                            expect(TrendingStrategy.fetchBucketedDataPromises[0].abort.calls.count()).toBe(1);
                        });
                    });

                    describe('and at least one of the fetches fails', function() {
                        beforeEach(function() {
                            TrendingStrategy.fetchBucketedDataPromises[0].reject(xhr);
                        });

                        it('should display an error message', function() {
                            expect(this.view.$('.trending-error')).not.toHaveClass('hide');
                        });

                        it('should hide the snap to now button', function() {
                            expect(this.view.$('.trending-snap-to-now')).toHaveClass('hide');
                        });

                        it('should hide the chart', function() {
                            expect(this.view.$('.trending-chart')).toHaveClass('hide');
                        });

                        it('should hide the slider', function() {
                            expect(this.view.$('.trending-slider')).toHaveClass('hide');
                        });
                    });

                    describe('and all the bucketed values fetches return successfully', function() {
                        beforeEach(function() {
                            TrendingStrategy.fetchBucketedDataPromises[0].resolve();
                        });

                        it('should create the trending chart data', function() {
                            expect(TrendingStrategy.createChartData.calls.count()).toBe(1);
                            expect(Trending.instances).toHaveLength(1);
                        });

                        it('should not display an error message', function() {
                            expect(this.view.$('.trending-error')).toHaveClass('hide');
                        });

                        it('should display the snap to now button', function() {
                            expect(this.view.$('.trending-snap-to-now')).not.toHaveClass('hide');
                        });

                        it('should display the chart', function() {
                            expect(this.view.$('.trending-chart')).not.toHaveClass('hide');
                        });

                        it('should draw the trending chart with the correct data', function() {
                            expect(Trending.instances[0].draw.calls.count()).toBe(1);
                            expect(Trending.instances[0].draw.calls.argsFor(0)[0].chartData.data).toHaveLength(2);
                            expect(Trending.instances[0].draw.calls.argsFor(0)[0].chartData.data[0].points).toHaveLength(1);
                            expect(typeof Trending.instances[0].draw.calls.argsFor(0)[0].zoomCallback).toBe('function');
                            expect(typeof Trending.instances[0].draw.calls.argsFor(0)[0].dragMoveCallback).toBe('function');
                            expect(typeof Trending.instances[0].draw.calls.argsFor(0)[0].dragEndCallback).toBe('function');
                        });

                        it('should show the slider', function() {
                            expect(this.view.$('.trending-slider')).not.toHaveClass('hide');
                        });

                        it('sets up the model correctly', function() {
                            expect(this.view.model.get('value')).toBe(DEFAULT_NUMBER_OF_BUCKETS);
                        });

                        it('should re-draw the graph after calling the zoom callback', function() {
                            Trending.instances[0].draw.calls.argsFor(0)[0].zoomCallback(1, 22);
                            expect(Trending.instances[0].draw.calls.count()).toBe(2);
                        });

                        it('should trigger a new fetch for bucketed values after calling the zoom callback', function() {
                            const count = TrendingStrategy.fetchBucketedDataPromises.length;
                            Trending.instances[0].draw.calls.argsFor(0)[0].zoomCallback(1, 22);
                            // In PhantomJS, fetchBucketedData() may be called multiple times because
                            // of how we mock out _.debounce()
                            expect(TrendingStrategy.fetchBucketedDataPromises.length).toBeGreaterThan(count);
                        });

                        describe('after calling the drag move callback', function() {
                            beforeEach(function() {
                                Trending.instances[0].draw.calls.argsFor(0)[0].dragMoveCallback(1, 22);
                            });

                            it('should re-draw the graph', function() {
                                expect(Trending.instances[0].draw.calls.count()).toBe(2);
                            });
                        });

                        it('should trigger a new fetch for bucketed values after calling the drag end callback', function() {
                            const count = TrendingStrategy.fetchBucketedDataPromises.length;
                            Trending.instances[0].draw.calls.argsFor(0)[0].dragEndCallback(1, 22);
                            // In PhantomJS, fetchBucketedData() may be called multiple times because
                            // of how we mock out _.debounce()
                            expect(TrendingStrategy.fetchBucketedDataPromises.length).toBeGreaterThan(count);
                        });

                        describe('after clicking the snap to now button', function() {
                            it('should trigger a new fetch for bucketed values', function() {
                                const count = TrendingStrategy.fetchBucketedDataPromises.length;
                                this.view.$('.trending-snap-to-now').click();
                                // In PhantomJS, fetchBucketedData() may be called multiple times because
                                // of how we mock out _.debounce()
                                expect(TrendingStrategy.fetchBucketedDataPromises.length).toBeGreaterThan(count);
                            });

                            it('should call for buckets with the right arguments', function() {
                                const tempTestMin = this.view.model.get('currentMin');
                                const tempTestMax = this.view.model.get('currentMax');
                                const tempTestNow = Date.now();
                                this.view.$('.trending-snap-to-now').click();

                                const args = TrendingStrategy.fetchBucketedData.calls.mostRecent().args[0];
                                expect(args.currentMin).toBe(tempTestMin);
                                expect(this.view.model.get('currentMin')).toBe(tempTestMin);
                                expect(this.view.model.get('currentMax')).not.toBe(tempTestMax);
                                // Expect currentMax to be within two seconds of reference tempTestNow
                                expect(args.currentMax - tempTestNow <= 2).toBe(true);
                            });
                        });
                    });
                });
            });
        });
    });
});
