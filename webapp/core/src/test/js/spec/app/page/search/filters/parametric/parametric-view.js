/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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
    'find/app/page/search/filters/parametric/parametric-view',
    'parametric-refinement/selected-values-collection'
], function(_, Backbone, ParametricView, SelectedValuesCollection) {
    'use strict';

    describe('Parametric view', function() {
        describe('when fields are returned', function() {
            beforeEach(function() {
                this.parametricFieldsCollection = new Backbone.Collection([{
                    id: '/DOCUMENT/WIKIPEDIA_CATEGORY',
                    displayName: 'Wikipedia Category',
                    type: 'Parametric',
                    totalValues: 121
                }, {
                    id: '/DOCUMENT/PERSON_SEX',
                    displayName: 'Person Sex',
                    type: 'Parametric',
                    totalValues: 131
                }, {
                    id: '/DOCUMENT/PLACE_ELEVATION',
                    displayName: 'Place Elevation (ft)',
                    max: 1084,
                    min: 5,
                    totalValues: 141,
                    currentMax: 1024,
                    currentMin: 8,
                    type: 'Numeric'
                }]);
                this.parametricFieldsCollection.isProcessing = _.noop;

                this.filteredParametricCollection = new Backbone.Collection([{
                    id: '/DOCUMENT/WIKIPEDIA_CATEGORY',
                    displayName: 'Wikipedia Category',
                    totalValues: 121,
                    values: [
                        {value: 'food', displayValue: 'food', count: 3},
                        {value: 'person', displayValue: 'person', count: 5}
                    ],
                    type: 'Parametric'
                }, {
                    id: '/DOCUMENT/PERSON_SEX',
                    displayName: 'Person Sex',
                    totalValues: 131,
                    values: [
                        {value: 'female', displayValue: 'female', count: 2}
                    ],
                    type: 'Parametric'
                }]);
                this.filteredParametricCollection.isProcessing = _.noop;

                this.selectedParametricValues = new SelectedValuesCollection();

                this.view = new ParametricView({
                    filterModel: new Backbone.Model(),
                    collection: this.parametricFieldsCollection,
                    parametricFieldsCollection: this.parametricFieldsCollection,
                    filteredParametricCollection: this.filteredParametricCollection,
                    queryState: {
                        selectedParametricValues: this.selectedParametricValues
                    }
                });

                this.view.render();
            });

            describe('when rendered with no request in flight', function() {
                it('displays every parametric value grouped by field', function() {
                    expect(this.view.$('[data-field]')).toHaveLength(2);

                    expect(this.view.$('[data-field="/DOCUMENT/WIKIPEDIA_CATEGORY"]')).toHaveLength(1);
                    expect(this.view.$('[data-field="/DOCUMENT/WIKIPEDIA_CATEGORY"] [data-value]')).toHaveLength(2);
                    expect(this.view.$('[data-field="/DOCUMENT/WIKIPEDIA_CATEGORY"] [data-value="food"]')).toHaveLength(1);
                    expect(this.view.$('[data-field="/DOCUMENT/WIKIPEDIA_CATEGORY"] [data-value="person"]')).toHaveLength(1);
                });

                it('hides the error message', function() {
                    expect(this.view.$('.parametric-fields-error')).toHaveClass('hide');
                });

                it('hides the empty message', function() {
                    expect(this.view.$('.parametric-fields-empty')).toHaveClass('hide');
                });

                it('displays the field list', function() {
                    expect(this.view.$('.parametric-fields-list')).not.toHaveClass('hide');
                });

                describe('then the parametric fields collection is fetched', function() {
                    beforeEach(function() {
                        this.parametricFieldsCollection.reset();
                        this.parametricFieldsCollection.trigger('request');
                    });

                    it('displays the loading spinner', function() {
                        expect(this.view.$('.parametric-fields-processing-indicator')).not.toHaveClass('hide');
                    });

                    it('hides the empty message', function() {
                        expect(this.view.$('.parametric-fields-empty')).toHaveClass('hide');
                    });

                    it('hides the field list', function() {
                        expect(this.view.$('.parametric-fields-list')).toHaveClass('hide');
                    });

                    describe('then the request fails', function() {
                        beforeEach(function() {
                            this.parametricFieldsCollection.trigger('error', this.parametricFieldsCollection, {status: 500});
                        });

                        it('hides the loading spinner', function() {
                            expect(this.view.$('.parametric-fields-processing-indicator')).toHaveClass('hide');
                        });

                        it('hides the empty message', function() {
                            expect(this.view.$('.parametric-fields-empty')).toHaveClass('hide');
                        });

                        it('hides the field list', function() {
                            expect(this.view.$('.parametric-fields-list')).toHaveClass('hide');
                        });
                    });

                    describe('then the request is aborted', function() {
                        beforeEach(function() {
                            this.parametricFieldsCollection.trigger('error', this.parametricFieldsCollection, {status: 0});
                        });

                        it('hides the error message', function() {
                            expect(this.view.$('.parametric-fields-error')).toHaveClass('hide');
                        });
                    });

                    describe('then the request succeeds', function() {
                        beforeEach(function() {
                            this.parametricFieldsCollection.reset([{
                                id: '/DOCUMENT/PERSON_SEX',
                                displayName: 'Person Sex',
                                type: 'Parametric',
                                totalValues: 131
                            }]);
                            this.parametricFieldsCollection.trigger('sync');
                        });

                        it('hides the loading spinner', function() {
                            expect(this.view.$('.parametric-fields-processing-indicator')).toHaveClass('hide');
                        });

                        it('hides the error message', function() {
                            expect(this.view.$('.parametric-fields-error')).toHaveClass('hide');
                        });

                        it('hides the empty message', function() {
                            expect(this.view.$('.parametric-fields-empty')).toHaveClass('hide');
                        });

                        it('displays the field list', function() {
                            expect(this.view.$('.parametric-fields-list')).not.toHaveClass('hide');
                        });
                    });
                });
            });
        });

        describe('when no fields are returned', function() {
            beforeEach(function() {
                this.filteredParametricCollection = new Backbone.Collection();

                const parametricFieldsCollection = new Backbone.Collection([]);
                parametricFieldsCollection.isProcessing = _.noop;
                this.view = new ParametricView({
                    filterModel: new Backbone.Model(),
                    collection: parametricFieldsCollection,
                    parametricFieldsCollection: parametricFieldsCollection,
                    filteredParametricCollection: this.filteredParametricCollection,
                    queryState: {
                        selectedParametricValues: this.selectedParametricValues
                    }
                });
            });

            it('shows the empty message', function() {
                expect(this.view.$('.parametric-fields-empty')).not.toHaveClass('hide');
            });
        })
    });
});
