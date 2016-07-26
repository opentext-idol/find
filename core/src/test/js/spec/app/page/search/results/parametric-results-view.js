/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/results/parametric-results-view',
    'backbone',
    'jasmine-ajax'
], function (ParametricResultsView, Backbone) {

    var DEPENDENT_EMPTY_MESSAGE = 'No dependent fields';
    var EMPTY_MESSAGE = 'No fields';
    var ERROR_MESSAGE = 'Error';

    describe('Parametric Results View', function() {
        beforeEach(function() {
            this.parametricCollection = new Backbone.Collection();
            this.selectedParametricValues = new Backbone.Collection();
            this.queryModel = new Backbone.Model();
            this.queryModel.getIsoDate = jasmine.createSpy('getIsoDate');
            this.queryState = {selectedParametricValues: this.selectedParametricValues};
            this.savedSearchModel = new Backbone.Model();

            var viewConstructorArguments = {
                emptyDependentMessage: DEPENDENT_EMPTY_MESSAGE,
                emptyMessage: EMPTY_MESSAGE,
                errorMessage: ERROR_MESSAGE,
                restrictedParametricCollection: this.parametricCollection,
                queryModel: this.queryModel,
                queryState: this.queryState,
                savedSearchModel: this.savedSearchModel

            };

            this.view = new ParametricResultsView(viewConstructorArguments);
            this.view.render();
        });

        describe('with an empty parametric collection', function() {
            it('should not display a loading spinner, content or field selections', function() {
                expect(this.view.$loadingSpinner).toHaveClass('hide');
                expect(this.view.$content).toHaveClass('invisible');
                expect(this.view.$parametricSelections).toHaveClass('hide');
            });

            describe('then the parametric collection fetches', function() {
                beforeEach(function() {
                    this.parametricCollection.fetching = true;
                    this.parametricCollection.trigger('request');
                });

                it('should not display a message, content view or field selections', function() {
                    expect(this.view.$message).toHaveText('');
                    expect(this.view.$content).toHaveClass('invisible');
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

                    it('should not display a loading spinner, content view or field selections', function() {
                        expect(this.view.$loadingSpinner).toHaveClass('hide');
                        expect(this.view.$content).toHaveClass('invisible');
                        expect(this.view.$parametricSelections).toHaveClass('hide');
                    });

                    it('should display the no parametric values for current search message', function() {
                        expect(this.view.$message).toHaveText(EMPTY_MESSAGE);
                    });
                });

                describe('then the parametric collection syncs and has errored', function() {
                    beforeEach(function () {
                        this.parametricCollection.fetching = false;
                        this.parametricCollection.error = true;
                        this.parametricCollection.trigger('error', null, {status: 1});
                    });

                    it('should not display a loading spinner, content view or field selections', function () {
                        expect(this.view.$loadingSpinner).toHaveClass('hide');
                        expect(this.view.$content).toHaveClass('invisible');
                        expect(this.view.$parametricSelections).toHaveClass('hide');
                    });

                    it('should display the no parametric values for current search message', function () {
                        expect(this.view.$message).toHaveText(ERROR_MESSAGE);
                    });
                });

                describe('then the parametric collection syncs and returns results', function() {
                    beforeEach(function() {
                        this.parametricCollection.fetching = false;

                        var sources = {
                            id: '/DOCUMENT/SOURCE',
                            field: 'SOURCE',
                            values: [
                                {
                                    value: 'GOOGLE',
                                    count: '89687'
                                },
                                {
                                    value: 'SPACE',
                                    count: '156235'
                                }
                            ]
                        };

                        var category = {
                            id: '/DOCUMENT/CATEGORY',
                            field: 'CATEGORY',
                            values: [
                                {
                                    value: 'SCIENCE',
                                    count: '43454'
                                }, {
                                    value: 'BUSINESS',
                                    count: '543534'
                                }, {
                                    value: 'COMPUTERS',
                                    count: '324663'
                                }
                            ]
                        };

                        var collectionContents = [sources, category];

                        this.parametricCollection.add(collectionContents);
                        this.parametricCollection.trigger('sync');

                        this.view.dependentParametricCollection.add(sources);
                        this.view.dependentParametricCollection.trigger('sync');
                    });

                    it('should not display a loading spinner or a message', function() {
                        expect(this.view.$loadingSpinner).toHaveClass('hide');
                        expect(this.view.$message).toHaveText('');
                    });

                    it('should display the dropdowns and the content view', function() {
                        expect(this.view.$parametricSelections).not.toHaveClass('hide');
                        expect(this.view.$content).not.toHaveClass('invisible');
                    });
                })
            });
        });
    })
});