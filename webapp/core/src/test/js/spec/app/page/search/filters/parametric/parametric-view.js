/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/page/search/filters/parametric/parametric-view',
    'find/app/util/merge-collection',
    'parametric-refinement/display-collection',
    'parametric-refinement/selected-values-collection'
], function(Backbone, ParametricView, MergeCollection, DisplayCollection, SelectedValuesCollection) {

    describe('Parametric view', function() {
        describe('when fields are returned', function () {
            beforeEach(function () {
                var models = [{
                    id: '/DOCUMENT/WIKIPEDIA_CATEGORY',
                    name: 'WIKIPEDIA_CATEGORY',
                    values: [
                        {value: 'food', count: 3},
                        {value: 'person', count: 5}
                    ],
                    dataType: 'parametric'
                }, {
                    id: '/DOCUMENT/PERSON_SEX',
                    name: 'PERSON_SEX',
                    values: [
                        {value: 'female', count: 2}
                    ],
                    dataType: 'parametric'
                }];

                var numericParametricModels = [{
                    id: '/DOCUMENT/PLACE_ELEVATION',
                    attributes: {
                        currentMax: 1024,
                        currentMin: 8,
                        dataType: 'numeric',
                        displayName: 'Place Elevation (ft)',
                        id: '/DOCUMENT/PLACE_ELEVATION',
                        max: 1084,
                        min: 5,
                        name: 'PLACE_ELEVATION',
                        totalValues: 141
                    },
                    dataType: 'numeric'
                }];

                this.parametricCollection = new Backbone.Collection(models);
                this.numericParametricCollection = new Backbone.Collection(numericParametricModels);

                this.selectedParametricValues = new SelectedValuesCollection();

                this.displayCollection = new DisplayCollection([], {
                    parametricCollection: this.parametricCollection,
                    selectedParametricValues: this.selectedParametricValues
                });

                this.mergedCollection = new MergeCollection([], {
                    collections: [this.numericParametricCollection, this.displayCollection],
                    typeAttribute: 'dataType'
                });

                this.view = new ParametricView({
                    filterModel: new Backbone.Model(),
                    collection: this.mergedCollection,
                    parametricCollection: this.parametricCollection,
                    displayCollection: this.displayCollection,
                    queryState: {
                        selectedParametricValues: this.selectedParametricValues
                    }
                });

                this.view.render();
            });

            describe('when rendered with no request in flight', function () {
                it('displays every parametric value grouped by field', function () {
                    expect(this.view.$('[data-field]')).toHaveLength(2);

                    expect(this.view.$('[data-field="/DOCUMENT/WIKIPEDIA_CATEGORY"]')).toHaveLength(1);
                    expect(this.view.$('[data-field="/DOCUMENT/WIKIPEDIA_CATEGORY"] [data-value]')).toHaveLength(2);
                    expect(this.view.$('[data-field="/DOCUMENT/WIKIPEDIA_CATEGORY"] [data-value="food"]')).toHaveLength(1);
                    expect(this.view.$('[data-field="/DOCUMENT/WIKIPEDIA_CATEGORY"] [data-value="person"]')).toHaveLength(1);
                });

                it('hides the error message', function () {
                    expect(this.view.$('.parametric-error')).toHaveClass('hide');
                });

                it('hides the empty message', function () {
                    expect(this.view.$('.parametric-empty')).toHaveClass('hide');
                });

                describe('then the parametric collection is fetched', function () {
                    beforeEach(function () {
                        this.parametricCollection.reset();
                        this.parametricCollection.trigger('request');
                    });

                    it('displays the loading spinner', function () {
                        expect(this.view.$('.parametric-processing-indicator')).not.toHaveClass('hide');
                    });

                    it('hides the empty message', function () {
                        expect(this.view.$('.parametric-empty')).toHaveClass('hide');
                    });

                    describe('then the request fails', function () {
                        beforeEach(function () {
                            this.parametricCollection.trigger('error', this.parametricCollection, {status: 500});
                        });

                        it('hides the loading spinner', function () {
                            expect(this.view.$('.parametric-processing-indicator')).toHaveClass('hide');
                        });

                        it('hides the empty message', function () {
                            expect(this.view.$('.parametric-empty')).toHaveClass('hide');
                        });
                    });

                    describe('then the request is aborted', function () {
                        beforeEach(function () {
                            this.parametricCollection.trigger('error', this.parametricCollection, {status: 0});
                        });

                        it('hides the error message', function () {
                            expect(this.view.$('.parametric-error')).toHaveClass('hide');
                        });
                    });

                    describe('then the request succeeds', function () {
                        beforeEach(function () {
                            this.parametricCollection.reset([{
                                id: '/DOCUMENT/PERSON_SEX',
                                name: 'PERSON_SEX',
                                values: [{value: 'male', count: 1}]
                            }]);
                            this.parametricCollection.trigger('sync');
                        });

                        it('hides the loading spinner', function () {
                            expect(this.view.$('.parametric-processing-indicator')).toHaveClass('hide');
                        });

                        it('hides the error message', function () {
                            expect(this.view.$('.parametric-error')).toHaveClass('hide');
                        });

                        it('hides the empty message', function () {
                            expect(this.view.$('.parametric-empty')).toHaveClass('hide');
                        });
                    });
                });
            });
        });

        describe('when no fields are returned', function(){
            beforeEach(function(){
                this.mergedCollection = new MergeCollection([], {
                    collections: [],
                    typeAttribute: 'dataType'
                });

                this.parametricCollection = new Backbone.Collection();
                this.displayCollection = new Backbone.Collection();
                this.parametricCollection = new Backbone.Collection();

                this.view = new ParametricView({
                    filterModel: new Backbone.Model(),
                    collection: this.mergedCollection,
                    parametricCollection: this.parametricCollection,
                    displayCollection: this.displayCollection,
                    queryState: {
                        selectedParametricValues: this.selectedParametricValues
                    }
                });
            });

            it('shows the empty message', function() {
                expect(this.view.$('.parametric-empty')).not.toHaveClass('hide');
            });
        })
    });

});
