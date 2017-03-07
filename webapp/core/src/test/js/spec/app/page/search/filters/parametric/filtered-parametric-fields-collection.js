/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/page/search/filters/parametric/filtered-parametric-fields-collection'
], function(Backbone, FilteredParametricFieldsCollection) {

    const Animal = {CAT: 'CAT', DOG: 'DOG', TIGER: 'TIGER', UNICORN: 'UNICORN'};

    const MockCollection = Backbone.Collection.extend({
        spy1: function () {
        },
        spy2: function () {
        },
        spy3: function () {
        }
    });

    describe('Filtered parametric fields collection', function() {
        beforeEach(function() {
            spyOn(FilteredParametricFieldsCollection.prototype, 'onAdd').and.callThrough();
            spyOn(FilteredParametricFieldsCollection.prototype, 'onRemove').and.callThrough();
            spyOn(FilteredParametricFieldsCollection.prototype, 'onChange').and.callThrough();
            spyOn(FilteredParametricFieldsCollection.prototype, 'onReset').and.callThrough();
            spyOn(FilteredParametricFieldsCollection.prototype, 'filterModels').and.callThrough();

            this.parametricFieldsCollection = new MockCollection([
                {id: 'Rambo', displayName: 'Rambo', value: Animal.CAT, displayValue: Animal.CAT, type: 'Parametric'},
                {id: 'Millie', displayName: 'Millie', value: Animal.CAT, displayValue: Animal.CAT, type: 'Parametric'},
                {id: 'Maisey', displayName: 'Maisey', value: Animal.DOG, displayValue: Animal.DOG, type: 'Parametric'},
                {id: 'Hobbes', displayName: 'Hobbes', value: Animal.TIGER, displayValue: Animal.TIGER, type: 'Parametric'}
            ]);

            this.filterModel = new Backbone.Model({text: ''});

            spyOn(this.parametricFieldsCollection, 'spy1').and.callThrough();
            spyOn(this.parametricFieldsCollection, 'spy2').and.callThrough();
            spyOn(this.parametricFieldsCollection, 'spy3').and.callThrough();

            this.filteredParametricFieldsCollection = new FilteredParametricFieldsCollection([], {
                collection: this.parametricFieldsCollection,
                filterModel: this.filterModel,
                collectionFunctions: ['spy1', 'spy2', 'spy3']
            });
        });

        it('should have 4 elements in the filtered collection', function() {
            expect(this.filteredParametricFieldsCollection.length).toBe(4);
        });

        describe('after filtering by the letter "M"', function() {
            beforeEach(function() {
                this.filterModel.set({text: 'm'});
            });

            it('should show 3 models when the filter text is set to "m"', function(){
                expect(this.filteredParametricFieldsCollection.length).toBe(3);
                expect(this.filteredParametricFieldsCollection.at(0)).toBe(this.parametricFieldsCollection.at(0));
                expect(this.filteredParametricFieldsCollection.at(1)).toBe(this.parametricFieldsCollection.at(1));
                expect(this.filteredParametricFieldsCollection.at(2)).toBe(this.parametricFieldsCollection.at(2));
            })
        });

        describe('after filtering by the letters "Hobbes"', function() {
            beforeEach(function() {
                this.filterModel.set({text: 'Hobbes'});
            });

            it('should show 1 model when the filter text is set to "Hobbes"', function(){
                expect(this.filteredParametricFieldsCollection.length).toBe(1);
                expect(this.filteredParametricFieldsCollection.at(0)).toBe(this.parametricFieldsCollection.at(3));
            })
        });

        describe('When calling the collection functions', function() {
            beforeEach(function() {
                this.filteredParametricFieldsCollection.spy1();
                this.filteredParametricFieldsCollection.spy2(1,2,3);
            });

            it('should have called the original functions', function(){
                expect(this.parametricFieldsCollection.spy1).toHaveBeenCalled();
                expect(this.parametricFieldsCollection.spy2).toHaveBeenCalled();
                expect(this.parametricFieldsCollection.spy3).not.toHaveBeenCalled();

                expect(this.parametricFieldsCollection.spy2.calls.argsFor(0)).toEqual([1,2,3]);
            })
        });

        describe('After adding a model to the original collection', function() {
            beforeEach(function() {
                this.parametricFieldsCollection.add(new Backbone.Model({id: 'Twilight', displayName: 'Twilight', value: Animal.UNICORN, displayValue: Animal.UNICORN, type: 'Parametric'}));
            });

            it('should have 5 models', function(){
                expect(this.filteredParametricFieldsCollection.length).toBe(5);
            });

            it('should have called onAdd', function(){
                expect(FilteredParametricFieldsCollection.prototype.onAdd).toHaveBeenCalled();
            });
        });

        describe('After removing a model from the original collection', function() {
            beforeEach(function() {
                this.parametricFieldsCollection.remove(this.parametricFieldsCollection.at(0));
            });

            it('filtering collection should have 3 models', function(){
                expect(this.filteredParametricFieldsCollection.length).toBe(3);
            });

            it('should have called onRemove', function(){
                expect(FilteredParametricFieldsCollection.prototype.onRemove).toHaveBeenCalled();
            });
        });

        describe('After changing a model in the original collection', function() {
            beforeEach(function() {
                this.parametricFieldsCollection.findWhere({displayName: 'Rambo'}).set({value: Animal.TIGER});
            });

            it('should update the model in the filtering collection', function(){
                expect(this.filteredParametricFieldsCollection.findWhere({displayName: 'Rambo'}).get('value')).toBe(Animal.TIGER);
            });

            it('should have called onChange', function(){
                expect(FilteredParametricFieldsCollection.prototype.onChange).toHaveBeenCalled();
            });
        });

        describe('After resetting the original collection', function() {
            beforeEach(function() {
                this.parametricFieldsCollection.reset([{id: 'Luna', displayName: 'Luna', value: Animal.TIGER, displayValue: Animal.TIGER, type: 'Parametric'}]);
            });

            it('should have called onReset', function(){
                expect(FilteredParametricFieldsCollection.prototype.onReset).toHaveBeenCalled();
            });

            it('should update the filtering collection', function(){
                expect(this.filteredParametricFieldsCollection.length).toBe(1);
            });

            it('should contain the new entry', function() {
                expect(this.filteredParametricFieldsCollection.at(0)).toBe(this.parametricFieldsCollection.at(0));
            });
        });
    });
});
