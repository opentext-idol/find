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
    'find/app/page/search/results/parametric-results-view',
    'backbone',
    'find/app/configuration',
    'parametric-refinement/selected-values-collection',
    'jasmine-ajax'
], function(ParametricResultsView, Backbone, configuration, SelectedParametricValues) {
    'use strict';

    const DEPENDENT_EMPTY_MESSAGE = 'No dependent fields';
    const EMPTY_MESSAGE = 'No fields';
    const ERROR_MESSAGE = 'Error';

    describe('Parametric Results View', function() {
        beforeEach(function() {
            this.parametricCollection = new Backbone.Collection();
            this.selectedParametricValues = new SelectedParametricValues();
            this.queryModel = new Backbone.Model();
            this.queryModel.getIsoDate = jasmine.createSpy('getIsoDate');
            this.queryState = {selectedParametricValues: this.selectedParametricValues};
            this.savedSearchModel = new Backbone.Model();

            const viewConstructorArguments = {
                emptyDependentMessage: DEPENDENT_EMPTY_MESSAGE,
                emptyMessage: EMPTY_MESSAGE,
                errorMessageArguments: {messageToUser: ERROR_MESSAGE},
                parametricCollection: this.parametricCollection,
                queryModel: this.queryModel,
                queryState: this.queryState,
                savedSearchModel: this.savedSearchModel
            };

            configuration.and.returnValue({
                errorCallSupportString: 'Custom call support message'
            });

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

                describe('then the parametric collection syncs with errors', function() {
                    beforeEach(function() {
                        this.parametricCollection.fetching = false;
                        this.parametricCollection.error = true;
                        this.parametricCollection.trigger('error', null, {status: 1});
                    });

                    it('should not display a loading spinner, content view, or field selections', function() {
                        expect(this.view.$loadingSpinner).toHaveClass('hide');
                        expect(this.view.$content).toHaveClass('invisible');
                        expect(this.view.$parametricSelections).toHaveClass('hide');
                    });

                    it('should display the "No parametric values for current search" message', function() {
                        expect(this.view.$errorMessage).toContainText(ERROR_MESSAGE);
                    });
                });

                describe('then the parametric collection syncs and returns results', function() {
                    beforeEach(function() {
                        this.parametricCollection.fetching = false;

                        const sources = {
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

                        const category = {
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

                        const collectionContents = [sources, category];

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

                    describe('then the selectors are populated', function () {
                        beforeEach(function() {
                             this.view.fieldsCollection.at(0).set({field: '/DOCUMENT/SOURCE', displayValue:'SOURCE'});
                             this.view.fieldsCollection.at(1).set({field: '/DOCUMENT/CATEGORY', displayValue:'CATEGORY'});
                        });


                        it('should enable the swap fields button', function() {
                            expect(this.view.$parametricSwapButton).not.toHaveClass('disabled');
                            expect(this.view.$parametricSwapButton).not.toBeDisabled();
                        });

                        describe('then the swap button is clicked', function(){
                            beforeEach(function(){
                                this.view.$parametricSwapButton.click();
                            });

                            it('should have swapped the fields', function() {
                                expect(this.view.fieldsCollection.at(0).get('field')).toBe('/DOCUMENT/CATEGORY');
                                expect(this.view.fieldsCollection.at(1).get('field')).toBe('/DOCUMENT/SOURCE');
                            });
                        });

                        describe('then the second field is removed', function(){
                            beforeEach(function () {
                                const second = this.view.fieldsCollection.at(1);
                                second.set('field', '');
                                this.view.fieldsCollection.set([second]);
                            });

                            it('should disable the swap fields button', function() {
                                expect(this.view.$parametricSwapButton).toHaveClass('disabled');
                                expect(this.view.$parametricSwapButton).toBeDisabled();
                            });
                        });
                    });
                });
            });
        });
    });
});
