/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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
    'find/app/page/search/filter-view',
    'find/app/configuration',
    'backbone',
    'i18n!find/nls/bundle',
    'find/app/model/document-selection-model',
    'js-testing/backbone-mock-factory'
], function(_, FilterView, configuration, Backbone, i18n, DocumentSelectionModel, backboneMockFactory) {
    'use strict';

    const MATCH_NOTHING = 'y54u65u4w5uy654u5eureuy654yht754wy54euy45';

    const MockIndexesView = Backbone.View.extend({
        initialize: function(options) {
            this.visibleIndexesCallback = options.visibleIndexesCallback;
        }
    });

    describe("Filter View", function() {
        beforeEach(function() {
            configuration.and.callFake(function() {
                return {
                    enableMetaFilter: true,
                    resultViewOrder: ["topic-map", "list"]
                };
            });

            this.queryModel = new Backbone.Model({
                editingDocumentSelection: false
            });

            const parametricFieldsCollection = new (backboneMockFactory.getCollection())();
            const parametricCollection = new (backboneMockFactory.getCollection())();
            this.view = new FilterView({
                IndexesView: MockIndexesView,
                queryModel: this.queryModel,
                queryState: {
                    geographyModel: new Backbone.Model({}),
                    documentSelectionModel: new DocumentSelectionModel()
                },
                parametricFieldsCollection: parametricFieldsCollection,
                parametricCollection: parametricCollection
            });

            this.view.render();
            this.view.$el.appendTo($('body'));

            this.parametricInfo = {
                description: 'parametric',
                collection: parametricFieldsCollection,
                view: this.view.parametricView
            };
        });

        afterEach(function() {
            this.view.remove();
        });

        describe('dates filter', function() {
            it('should not display when the filter does not match the bundle string for dates', function() {
                this.view.filterModel.set('text', MATCH_NOTHING);

                expect(this.view.dateViewWrapper.$el).toHaveClass('hide');
            });

            it('should display when the filter is the empty string', function() {
                this.view.filterModel.set('text', '');

                expect(this.view.dateViewWrapper.$el).not.toHaveClass('hide');
                expect(this.view.$emptyMessage).toHaveClass('hide');
            });

            it('should display when the filter incompletely matches the bundle string for dates', function() {
                this.view.filterModel.set('text', i18n['search.dates'].substring(0, 3));

                expect(this.view.dateViewWrapper.$el).not.toHaveClass('hide');
                expect(this.view.$emptyMessage).toHaveClass('hide');
            });
        });

        describe('indexes filter', function() {
            it('should display when the visibleIndexesCallback is called with a non-empty array', function() {
                this.view.indexesViewWrapper.view.visibleIndexesCallback(['index1', 'index2', 'index3']);

                expect(this.view.indexesViewWrapper.view.$el).not.toHaveClass('hide');
                expect(this.view.$emptyMessage).toHaveClass('hide');

            });

            it('should not display when the visibleIndexesCallback is called with the empty array', function() {
                this.view.indexesViewWrapper.view.visibleIndexesCallback([]);

                expect(this.view.indexesViewWrapper.$el).toHaveClass('hide');
            });
        });

        describe('Document selection filter', function () {
            // setTimeout calls are because collapsing has a transition state that takes time

            beforeEach(function () {
                this.collapse = this.view.documentSelectionViewWrapper;
            });

            it('should show the filter view collapsed', function () {
                expect(this.collapse.$el.is(':visible')).toBe(true);
                expect(this.collapse.$('.collapse')).not.toHaveClass('in');
            });

            describe('when editing the document selection starts', function () {

                beforeEach(function (done) {
                    this.queryModel.set('editingDocumentSelection', true);
                    setTimeout(done, 500);
                });

                it('should expand the filter view', function () {
                    expect(this.collapse.$('.collapse')).toHaveClass('in');
                });

                describe('then editing the document selection stops', function () {

                    beforeEach(function (done) {
                        this.queryModel.set('editingDocumentSelection', false);
                        setTimeout(done, 500);
                    });

                    it('should collapse the filter view', function () {
                        expect(this.collapse.$('.collapse')).not.toHaveClass('in');
                    });

                });

            });

            describe('when the view header is clicked', function () {

                beforeEach(function (done) {
                    this.collapse.$('.collapsible-header').click();
                    setTimeout(done, 500);
                });

                it('should start editing the document selection', function () {
                    expect(this.queryModel.get('editingDocumentSelection')).toBe(true);
                });

                describe('then the view header is clicked again', function () {

                    beforeEach(function (done) {
                        this.collapse.$('.collapsible-header').click();
                        setTimeout(done, 500);
                    });

                    it('should stop editing the document selection', function () {
                        expect(this.queryModel.get('editingDocumentSelection')).toBe(false);
                    });

                });

            });

        });

        describe('Parametric values filter', function () {
            beforeEach(function () {
                this.parametricInfo.collection.reset();
            });

            describe('with parametric values and the filter set to the empty string', function () {
                it('should display when the displayCollection is not empty', function () {
                    this.parametricInfo.collection.add({fakeAttribute: true});

                    expect(this.parametricInfo.view.$el).not.toHaveClass('hide');
                    expect(this.view.$emptyMessage).toHaveClass('hide');
                });

                it('should not be displayed when there are no parametric values matching the filter', function () {
                    this.view.filterModel.set('text', MATCH_NOTHING);

                    expect(this.parametricInfo.view.$el).toHaveClass('hide');
                });
            });

            describe('with no parametric values', function () {
                it('should display when the filter is empty', function () {
                    this.view.filterModel.set('text', '');

                    expect(this.parametricInfo.view.$el).not.toHaveClass('hide');
                    expect(this.view.$emptyMessage).toHaveClass('hide');
                });

                it('should not display when the filter is non-empty', function () {
                    this.view.filterModel.set('text', MATCH_NOTHING);

                    expect(this.parametricInfo.view.$el).toHaveClass('hide');
                });
            });
        });

        it('should display the no filters matched message and hide everything when no filters are matched', function() {
            this.view.indexesViewWrapper.view.visibleIndexesCallback([]);
            this.view.filterModel.set('text', MATCH_NOTHING);

            expect(this.view.dateViewWrapper.$el).toHaveClass('hide');
            expect(this.view.indexesViewWrapper.$el).toHaveClass('hide');
            expect(this.view.parametricView.$el).toHaveClass('hide');
            expect(this.view.$emptyMessage).not.toHaveClass('hide');
        });

        it('should track the collapsible state of the indexes view', function () {
            spyOn(this.view.indexesViewWrapper, 'toggle');

            expect(this.view.collapsed.indexes).toBe(true);

            this.view.indexesViewWrapper.$('.collapsible-header').click();
            this.view.filterModel.set('text', 'ind');

            this.view.indexesViewWrapper.view.visibleIndexesCallback(['index1']);

            // this shouldn't change for auto toggle
            expect(this.view.collapsed.indexes).toBe(false);

            this.view.filterModel.set('text', '');
            this.view.indexesViewWrapper.view.visibleIndexesCallback(['index1']);

            expect(this.view.indexesViewWrapper.toggle.calls.count()).toBe(2);
            expect(this.view.indexesViewWrapper.toggle.calls.argsFor(0)[0]).toBeTruthy();
            expect(this.view.indexesViewWrapper.toggle.calls.argsFor(1)[0]).toBe(true);
        });

        it('should track the collapsible state of the date view', function () {
            spyOn(this.view.dateViewWrapper, 'toggle');

            expect(this.view.collapsed.dates).toBe(true);

            this.view.dateViewWrapper.$('.collapsible-header').click();
            this.view.filterModel.set('text', 'ind');

            expect(this.view.dateViewWrapper.toggle.calls.count()).toBe(1);

            this.view.filterModel.set('text', '');

            expect(this.view.dateViewWrapper.toggle.calls.count()).toBe(2);
            expect(this.view.dateViewWrapper.toggle.calls.argsFor(0)[0]).toBeTruthy();
            expect(this.view.dateViewWrapper.toggle.calls.argsFor(1)[0]).toBe(true);
        });
    });
});
