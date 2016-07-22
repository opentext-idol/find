/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/page/search/filters/parametric/parametric-view',
    'parametric-refinement/display-collection',
    'parametric-refinement/selected-values-collection'
], function(Backbone, ParametricView, DisplayCollection, SelectedValuesCollection) {

    describe('Parametric view', function() {
        beforeEach(function() {
            var models = [
                {id: '/DOCUMENT/WIKIPEDIA_CATEGORY', name: 'WIKIPEDIA_CATEGORY', values: [{value: 'food', count: 3}, {value: 'person', count: 5}]},
                {id: '/DOCUMENT/PERSON_SEX', name: 'PERSON_SEX', values: [{value: 'female', count: 2}]}
            ];

            this.restrictedParametricCollection = new Backbone.Collection(models);

            this.selectedParametricValues = new SelectedValuesCollection();

            var displayCollection = new DisplayCollection([], {
                parametricCollection: this.restrictedParametricCollection,
                selectedParametricValues: this.selectedParametricValues
            });

            this.view = new ParametricView({
                parametricCollection: new Backbone.Collection(models),
                restrictedParametricCollection: this.restrictedParametricCollection,
                displayCollection: displayCollection,
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

                expect(this.view.$('[data-field="/DOCUMENT/PERSON_SEX"]')).toHaveLength(1);
                expect(this.view.$('[data-field="/DOCUMENT/PERSON_SEX"] [data-value]')).toHaveLength(1);
                expect(this.view.$('[data-field="/DOCUMENT/PERSON_SEX"] [data-value="female"]')).toHaveLength(1);
            });

            it('hides the loading spinner', function() {
                expect(this.view.$('.parametric-processing-indicator')).toHaveClass('hide');
            });

            it('hides the error message', function() {
                expect(this.view.$('.parametric-error')).toHaveClass('hide');
            });

            it('hides the empty message', function() {
                expect(this.view.$('.parametric-empty')).toHaveClass('hide');
            });

            describe('then the parametric collection is reset to empty', function() {
                beforeEach(function() {
                    this.restrictedParametricCollection.reset();
                });

                it('shows the empty message', function() {
                    expect(this.view.$('.parametric-empty')).not.toHaveClass('hide');
                });
            });

            describe('then the parametric collection is fetched', function() {
                beforeEach(function() {
                    this.restrictedParametricCollection.reset();
                    this.restrictedParametricCollection.trigger('request');
                });

                it('displays the loading spinner', function() {
                    expect(this.view.$('.parametric-processing-indicator')).not.toHaveClass('hide');
                });

                it('hides the empty message', function() {
                    expect(this.view.$('.parametric-empty')).toHaveClass('hide');
                });

                describe('then the request fails', function() {
                    beforeEach(function() {
                        this.restrictedParametricCollection.trigger('error', this.restrictedParametricCollection, {status: 500});
                    });

                    it('hides the loading spinner', function() {
                        expect(this.view.$('.parametric-processing-indicator')).toHaveClass('hide');
                    });

                    it('displays the error message', function() {
                        expect(this.view.$('.parametric-error')).not.toHaveClass('hide');
                    });

                    it('hides the empty message', function() {
                        expect(this.view.$('.parametric-empty')).toHaveClass('hide');
                    });
                });

                describe('then the request is aborted', function() {
                    beforeEach(function() {
                        this.restrictedParametricCollection.trigger('error', this.restrictedParametricCollection, {status: 0});
                    });

                    it('hides the error message', function() {
                        expect(this.view.$('.parametric-error')).toHaveClass('hide');
                    });
                });

                describe('then the request succeeds', function() {
                    beforeEach(function() {
                        this.restrictedParametricCollection.reset([{id: '/DOCUMENT/PERSON_SEX', name: 'PERSON_SEX', values: [{value: 'male', count: 1}]}]);
                        this.restrictedParametricCollection.trigger('sync');
                    });

                    it('hides the loading spinner', function() {
                        expect(this.view.$('.parametric-processing-indicator')).toHaveClass('hide');
                    });

                    it('hides the error message', function() {
                        expect(this.view.$('.parametric-error')).toHaveClass('hide');
                    });

                    it('hides the empty message', function() {
                        expect(this.view.$('.parametric-empty')).toHaveClass('hide');
                    });
                });
            });
        });
    });

});
