/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/results/sunburst-view',
    'backbone',
    'i18n!find/nls/bundle'
], function (SunburstView, Backbone, i18n) {

    describe('Sunburst View', function() {
        beforeEach(function() {
            this.parametricCollection = new Backbone.Collection();
            this.queryModel = new Backbone.Model();
            this.queryModel.getIsoDate = jasmine.createSpy('getIsoDate');

            var sunburstViewConstructorArguments = {
                parametricCollection: this.parametricCollection,
                queryModel: this.queryModel
            };

            this.view = new SunburstView(sunburstViewConstructorArguments);
            this.view.render();
        });

        describe('with an empty parametric collection', function() {
            it('should not display a loading spinner, sunburst view or field selections', function() {
                expect(this.view.$loadingSpinner).toHaveClass('hide');
                expect(this.view.$sunburst).toHaveClass('hide');
                expect(this.view.$parametricSelections).toHaveClass('hide');
            });

            it('should display the no parametric values for current search message', function() {
                expect(this.view.$message).toHaveText(i18n['search.resultsView.sunburst.error.noParametricValues']);
            });

            describe('then the parametric collection fetches', function() {
                beforeEach(function() {
                    this.parametricCollection.fetching = true;
                    this.parametricCollection.trigger('request');
                });

                it('should not display a message, sunburst view or field selections', function() {
                    expect(this.view.$message).toHaveText('');
                    expect(this.view.$sunburst).toHaveClass('hide');
                    expect(this.view.$parametricSelections).toHaveClass('hide');
                });

                it('should display a loading spinner', function() {
                    expect(this.view.$loadingSpinner).not.toHaveClass('hide');
                });

                describe('then the parametric collection syncs and is empty', function() {
                    beforeEach(function() {
                        this.parametricCollection.fetching = false;
                        this.parametricCollection.trigger('sync');
                    });

                    it('should not display a loading spinner, sunburst view or field selections', function() {
                        expect(this.view.$loadingSpinner).toHaveClass('hide');
                        expect(this.view.$sunburst).toHaveClass('hide');
                        expect(this.view.$parametricSelections).toHaveClass('hide');
                    });

                    it('should display the no parametric values for current search message', function() {
                        expect(this.view.$message).toHaveText(i18n['search.resultsView.sunburst.error.noParametricValues']);
                    });
                });

                describe('then the parametric collection syncs and has errored', function() {
                    beforeEach(function () {
                        this.parametricCollection.fetching = false;
                        this.parametricCollection.error = true;
                        this.parametricCollection.trigger('error');
                    });

                    it('should not display a loading spinner, sunburst view or field selections', function () {
                        expect(this.view.$loadingSpinner).toHaveClass('hide');
                        expect(this.view.$sunburst).toHaveClass('hide');
                        expect(this.view.$parametricSelections).toHaveClass('hide');
                    });

                    it('should display the no parametric values for current search message', function () {
                        expect(this.view.$message).toHaveText(i18n['search.resultsView.sunburst.error.query']);
                    });
                });
            });
        });
    })
});