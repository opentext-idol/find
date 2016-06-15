/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/page/search/abstract-query-left-side-view',
    'backbone',
    'i18n!find/nls/bundle',
    'js-testing/backbone-mock-factory'
], function (_, AbstractQueryLeftSideView, Backbone, i18n, backboneMockFactory) {
    "use strict";

    var MATCH_NOTHING = 'y54u65u4w5uy654u5eureuy654yht754wy54euy45';

    var types = ['parametric', 'numeric parametric', 'date parametric'];

    describe("Abstract Query Left Side View", function () {

        beforeEach(function () {
            this.view = new (AbstractQueryLeftSideView.extend({
                IndexesView: Backbone.View.extend({
                    initialize: function (options) {
                        this.visibleIndexesCallback = options.visibleIndexesCallback;
                    }
                }),
                NumericParametricView: Backbone.View
            }))({
                queryState: {},
                numericParametricFieldsCollection: new (backboneMockFactory.getCollection())(),
                dateParametricFieldsCollection: new (backboneMockFactory.getCollection())()
            });

            this.parametricInfo = {
                parametric: {
                    description: 'parametric',
                    collection: this.view.parametricDisplayCollection,
                    view: this.view.numericParametricView
                },
                'numeric parametric': {
                    description: 'numeric parametric',
                    collection: this.view.numericParametricFieldsCollection,
                    view: this.view.parametricView
                },
                'date parametric': {
                    description: 'date parametric',
                    collection: this.view.dateParametricFieldsCollection,
                    view: this.view.dateParametricView
                }
            };
        });

        describe('dates filter', function () {
            it('should not display when the filter does not match the bundle string for dates', function () {
                this.view.filterModel.set('text', MATCH_NOTHING);

                expect(this.view.dateViewWrapper.$el).toHaveClass('hide');

            });

            it('should display when the filter is the empty string', function () {
                this.view.filterModel.set('text', '');

                expect(this.view.dateViewWrapper.$el).not.toHaveClass('hide');
                expect(this.view.$emptyMessage).toHaveClass('hide');
            });

            it('should display when the filter incompletely matches the bundle string for dates', function () {
                this.view.filterModel.set('text', i18n['search.dates'].substring(0, 3));

                expect(this.view.dateViewWrapper.$el).not.toHaveClass('hide');
                expect(this.view.$emptyMessage).toHaveClass('hide');
            });
        });

        describe('indexes filter', function () {
            it('should display when the visibleIndexesCallback is called with a non-empty array', function () {
                this.view.indexesViewWrapper.view.visibleIndexesCallback(['index1', 'index2', 'index3']);

                expect(this.view.indexesViewWrapper.view.$el).not.toHaveClass('hide');
                expect(this.view.$emptyMessage).toHaveClass('hide');

            });

            it('should not display when the visibleIndexesCallback is called with the empty array', function () {
                this.view.indexesViewWrapper.view.visibleIndexesCallback([]);

                expect(this.view.indexesViewWrapper.$el).toHaveClass('hide');
            });
        });

        types.forEach(function (type) {
            describe(type + ' values filter', function () {
                beforeEach(function () {
                    this.parametricInfo[type].collection.reset();
                });

                describe('with parametric values and the filter set to the empty string', function () {
                    beforeEach(function () {
                        this.view.filterModel.set('');
                    });

                    it('should display when the displayCollection is not empty', function () {
                        this.parametricInfo[type].collection.add({fakeAttribute: true});

                        expect(this.parametricInfo[type].view.$el).not.toHaveClass('hide');
                        expect(this.view.$emptyMessage).toHaveClass('hide');
                    });

                    it('should not be displayed when there are no parametric values matching the filter', function () {
                        this.view.filterModel.set('text', MATCH_NOTHING);

                        expect(this.parametricInfo[type].view.$el).toHaveClass('hide');
                    })
                });

                describe('with no parametric values', function () {
                    it('should display when the filter is empty', function () {
                        this.view.filterModel.set('text', '');

                        expect(this.parametricInfo[type].view.$el).not.toHaveClass('hide');
                        expect(this.view.$emptyMessage).toHaveClass('hide');
                    });

                    it('should not display when the filter is non-empty', function () {
                        this.view.filterModel.set('text', MATCH_NOTHING);

                        expect(this.parametricInfo[type].view.$el).toHaveClass('hide');
                    });
                })
            });
        }, this);

        it('should display the no filters matched message and hide everything when no filters are matched', function () {
            this.view.indexesViewWrapper.view.visibleIndexesCallback([]);
            this.view.filterModel.set('text', MATCH_NOTHING);

            expect(this.view.dateViewWrapper.$el).toHaveClass('hide');
            expect(this.view.indexesViewWrapper.$el).toHaveClass('hide');
            expect(this.view.parametricView.$el).toHaveClass('hide');
            expect(this.view.$emptyMessage).not.toHaveClass('hide');
        });
    });

});
