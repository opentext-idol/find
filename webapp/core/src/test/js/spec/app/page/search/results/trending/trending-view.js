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
    'find/app/page/search/results/trending/trending-view',
    'mock/page/results/trending'
], function (_, $, Backbone, i18n, backboneMockFactory, configuration, ParametricCollection, ParametricDetailsModel,
             BucketedParametricCollection, TrendingView, Trending) {
    'use strict';

    const originalDebounce = _.debounce;

    const xhr = {
        status: 500,
        responseJSON: {
            message: 'error',
            uuid: '1234'
        }
    };

    describe('Trending view', function () {
        beforeEach(function () {
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

            const queryState = {
                selectedParametricValues: new Backbone.Collection()
            };

            this.parametricCollection = new Backbone.Collection([
                {
                    id: 'cheeses',
                    totalValues: 200
                }, {
                    id: 'breads',
                    totalValues: 450
                }, {
                    id: 'meats',
                    totalValues: 140
                }, {
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
                }, {
                    id: 'breads',
                    displayName: 'Breads',
                    totalValues: 1000,
                    type: 'Parametric'
                }, {
                    id: 'meats',
                    displayName: 'Meats',
                    totalValues: 1000,
                    type: 'Parametric'
                }, {
                    id: 'vegetables',
                    displayName: 'Veg',
                    totalValues: 1000,
                    type: 'Parametric'
                }, {
                    id: 'numeric to be discarded',
                    displayName: '',
                    totalValues: 38,
                    type: 'Numeric'
                }
            ]);

            configuration.and.returnValue({
                trending: {
                    dateField: "AUTN_DATE",
                    numberOfBuckets: 20,
                    numberOfValues: 10
                }
            });

            this.view = new TrendingView({
                queryModel: this.queryModel,
                queryState: queryState,
                parametricCollection: this.parametricCollection,
                parametricFieldsCollection: this.parametricFieldsCollection
            });

            $(document.body).append(this.view.$el);
            this.view.render();
        });

        afterEach(function () {
            this.view.remove();
            _.debounce = originalDebounce;
            ParametricCollection.reset();
            ParametricDetailsModel.reset();
            BucketedParametricCollection.Model.reset();
            Trending.reset();
        });

        it('should render the template html', function () {
            expect(this.view.$('.trending-chart').length).toBe(1);
        });

        describe('and when the parametric collection syncs', function () {
            beforeEach(function () {
                this.parametricCollection.trigger('sync');
            });

            it('should render and populate the field selector', function () {
                const $option = this.view.$('.trending-field-selector option');
                expect($option.length).toBe(4);
                expect($option[0].text).toBe('Cheeses (200)');
                expect($option[1].text).toBe('Breads (450)');
                expect($option[2].text).toBe('Meats (140)');
                expect($option[3].text).toBe('Veg (223)');
            });

            it('should call fetch on the parametric collection with the correct arguments', function () {
                expect(ParametricCollection.instances[0].fetch).toHaveBeenCalled();
                // fieldNames set from the automatically selected first field in dropdown
                expect(ParametricCollection.instances[0].fetch.calls.argsFor(0)[0].data.fieldNames).toEqual(['cheeses']);
                expect(ParametricCollection.instances[0].fetch.calls.argsFor(0)[0].data.queryText).toBe(this.queryModel.get('queryText'));
            });

            describe('and the fetch fails', function () {
                beforeEach(function () {
                    ParametricCollection.instances[0].fetch.calls.argsFor(0)[0].error([], xhr);
                });

                it('should display an error message', function () {
                    expect(this.view.$('.trending-error')).not.toHaveClass('hide');
                });

                it('should hide the chart', function () {
                    expect(this.view.$('.trending-chart')).toHaveClass('hide');
                });
            });

            describe('and the fetch succeeds', function () {
                beforeEach(function () {
                    ParametricCollection.instances[0].set({
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
                    });
                    ParametricCollection.instances[0].fetch.calls.argsFor(0)[0].success();
                });

                it('should fetch range data with the correct arguments', function () {
                    expect(ParametricDetailsModel.instances[0].fetch).toHaveBeenCalled();
                    expect(ParametricDetailsModel.instances[0].fetch.calls.argsFor(0)[0].data.fieldName)
                        .toBe('AUTN_DATE');
                    expect(ParametricDetailsModel.instances[0].fetch.calls.argsFor(0)[0].data.fieldText)
                        .toBe('MATCH{CHEDDAR,STILTON,BRIE,RED LEICESTER}:cheeses');
                });

                describe('and the fetch for range details fails', function () {
                    beforeEach(function () {
                        ParametricDetailsModel.instances[0].fetch.calls.argsFor(0)[0].error([], xhr);
                    });

                    it('should display an error message', function () {
                        expect(this.view.$('.trending-error')).not.toHaveClass('hide');
                    });

                    it('should hide the chart', function () {
                        expect(this.view.$('.trending-chart')).toHaveClass('hide');
                    });
                });

                describe('and the fetch for range details succeeds', function () {
                    beforeEach(function () {
                        ParametricDetailsModel.instances[0].set({
                            min: 0,
                            max: 20
                        });
                        ParametricDetailsModel.instances[0].fetch.calls.argsFor(0)[0].success();
                    });

                    it('should make the correct number of bucketed values models', function () {
                        expect(BucketedParametricCollection.Model.instances.length).toBe(4);
                        expect(BucketedParametricCollection.Model.instances[0].constructorArgs[0]).toEqual({
                            id: 'AUTN_DATE',
                            valueName: 'CHEDDAR'
                        });
                        expect(BucketedParametricCollection.Model.instances[1].constructorArgs[0]).toEqual({
                            id: 'AUTN_DATE',
                            valueName: 'STILTON'
                        });
                        expect(BucketedParametricCollection.Model.instances[2].constructorArgs[0]).toEqual({
                            id: 'AUTN_DATE',
                            valueName: 'BRIE'
                        });
                        expect(BucketedParametricCollection.Model.instances[3].constructorArgs[0]).toEqual({
                            id: 'AUTN_DATE',
                            valueName: 'RED LEICESTER'
                        });
                    });

                    it('should trigger a fetch on each of these models', function () {
                        expect(BucketedParametricCollection.Model.fetchPromises.length).toBe(4);
                    });

                    describe('and one bucketed values fetch fails', function () {
                        beforeEach(function () {
                            BucketedParametricCollection.Model.fetchPromises[0].reject(xhr);
                        });

                        it('should display an error message', function () {
                            expect(this.view.$('.trending-error')).not.toHaveClass('hide');
                        });

                        it('should hide the chart', function () {
                            expect(this.view.$('.trending-chart')).toHaveClass('hide');
                        });
                    });

                    describe('and all the bucketed values fetches return successfully', function () {
                        beforeEach(function () {
                            _.each(BucketedParametricCollection.Model.instances, function (model) {
                                model.set({
                                    count: 2,
                                    displayName: model.get('valueName'),
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
                                })
                            });
                            _.each(BucketedParametricCollection.Model.fetchPromises, function (promise) {
                                promise.resolve();
                            });
                        });

                        it('should create the trending chart', function () {
                            expect(Trending.instances.length).toBe(1);
                        });

                        it('should draw the trending chart with the correct data', function () {
                            expect(Trending.instances[0].draw.calls.all().length).toBe(1);
                            expect(Trending.instances[0].draw.calls.argsFor(0)[0].data.length).toBe(4);
                            expect(Trending.instances[0].draw.calls.argsFor(0)[0].data[0].points.length).toBe(4);
                            expect(typeof Trending.instances[0].draw.calls.argsFor(0)[0].zoomCallback).toBe('function');
                            expect(typeof Trending.instances[0].draw.calls.argsFor(0)[0].dragMoveCallback).toBe('function');
                            expect(typeof Trending.instances[0].draw.calls.argsFor(0)[0].dragEndCallback).toBe('function');
                        });

                        describe('after calling the zoom callback', function () {
                            beforeEach(function () {
                                Trending.instances[0].draw.calls.argsFor(0)[0].zoomCallback(1, 22);
                            });

                            it('should re-draw the graph', function () {
                                expect(Trending.instances[0].draw.calls.all().length).toBe(2);
                            });

                            it('should trigger a new fetch for bucketed values', function () {
                                expect(BucketedParametricCollection.Model.instances.length).toBe(8);
                            });
                        });

                        describe('after calling the drag move callback', function () {
                            beforeEach(function () {
                                Trending.instances[0].draw.calls.argsFor(0)[0].dragMoveCallback(1, 22);
                            });

                            it('should re-draw the graph', function () {
                                expect(Trending.instances[0].draw.calls.all().length).toBe(2);
                            });
                        });

                        describe('after calling the drag end callback', function () {
                            beforeEach(function () {
                                Trending.instances[0].draw.calls.argsFor(0)[0].dragEndCallback(1, 22);
                            });

                            it('should trigger a new fetch for bucketed values', function () {
                                expect(BucketedParametricCollection.Model.instances.length).toBe(8);
                            });
                        });
                    });
                });
            });
        });
    });
});