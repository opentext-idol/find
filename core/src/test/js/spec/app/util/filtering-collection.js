/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/util/filtering-collection'
], function(Backbone, FilteringCollection) {

    var Animal = {CAT: 'CAT', DOG: 'DOG', TIGER: 'TIGER', UNICORN: 'UNICORN'};

    var MockCollection = Backbone.Collection.extend({
        spy1: function() {},
        spy2: function() {},
        spy3: function() {}
    });

    describe('Filtering collection', function() {
        beforeEach(function() {
            spyOn(FilteringCollection.prototype, 'onAdd').and.callThrough();
            spyOn(FilteringCollection.prototype, 'onRemove').and.callThrough();
            spyOn(FilteringCollection.prototype, 'onChange').and.callThrough();
            spyOn(FilteringCollection.prototype, 'onReset').and.callThrough();
            spyOn(FilteringCollection.prototype, 'filterModels').and.callThrough();

            this.petCollection = new MockCollection([
                {id: 0, name: 'Rambo', animal: Animal.CAT},
                {id: 1, name: 'Millie', animal: Animal.CAT},
                {id: 2, name: 'Maisey', animal: Animal.DOG},
                {id: 3, name: 'Hobbes', animal: Animal.TIGER}

            ]);

            this.filterModel = new Backbone.Model({text: '', filterOn: 'name'});

            this.filteringCollection = new FilteringCollection([], {
                collection: this.petCollection,
                filterModel: this.filterModel,
                predicate: function(model, filterModel) {
                    var filterOn = filterModel.get('filterOn');
                    return model.get(filterOn).toLowerCase().indexOf(filterModel.get('text').toLowerCase()) > -1;
                },
                resetOnFilter: false,
                collectionFunctions: ['spy1', 'spy2', 'spy3']
            });
            spyOn(this.petCollection, 'spy1').and.callThrough();
            spyOn(this.petCollection, 'spy2').and.callThrough();
            spyOn(this.petCollection, 'spy3').and.callThrough();
        });

        it('should have 4 elements in the filtered collection', function() {
            expect(this.filteringCollection.length).toBe(4);
        });

        describe('after filtering by the letter "M"', function() {
            beforeEach(function() {
                this.filterModel.set({text: 'm', filterOn: 'name'});
            });

            it('should show 3 models when the filter text is set to "m"', function(){
                expect(this.filteringCollection.length).toBe(3);
                expect(this.filteringCollection.at(0)).toBe(this.petCollection.at(0));
                expect(this.filteringCollection.at(1)).toBe(this.petCollection.at(1));
                expect(this.filteringCollection.at(2)).toBe(this.petCollection.at(2));
            })
        });

        describe('after filtering by the letters "Hobbes"', function() {
            beforeEach(function() {
                this.filterModel.set({text: 'Hobbes', filterOn: 'name'});
            });

            it('should show 1 model when the filter text is set to "Hobbes"', function(){
                expect(this.filteringCollection.length).toBe(1);
                expect(this.filteringCollection.at(0)).toBe(this.petCollection.at(3));
            })
        });

        describe('after filtering by the animal type "Tiger"', function() {
            beforeEach(function() {
                this.filterModel.set({text: 'TIGER', filterOn: 'animal'});
            });

            it('should show 1 model when the filter text is set to "Tiger"', function(){
                expect(this.filteringCollection.length).toBe(1);
                expect(this.filteringCollection.at(0)).toBe(this.petCollection.at(3));
            })
        });

        describe('When calling the collection functions', function() {
            beforeEach(function() {
                this.filteringCollection.spy1();
                this.filteringCollection.spy2();
            });

            it('should have called the original functions', function(){
                expect(this.petCollection.spy1).toHaveBeenCalled();
                expect(this.petCollection.spy2).toHaveBeenCalled();
                expect(this.petCollection.spy3).not.toHaveBeenCalled();
            })
        });

        describe('After adding a model to the original collection', function() {
            beforeEach(function() {
                this.petCollection.add(new Backbone.Model({name: 'Twilight', animal: Animal.UNICORN}));
            });

            it('should have 5 models', function(){
                expect(this.filteringCollection.length).toBe(5);
            });

            it('should have called onAdd', function(){
                expect(FilteringCollection.prototype.onAdd).toHaveBeenCalled();
            });
        });

        describe('After removing a model from the original collection', function() {
            beforeEach(function() {
                this.petCollection.remove(this.petCollection.at(0));
            });

            it('filtering collection should have 3 models', function(){
                expect(this.filteringCollection.length).toBe(3);
            });

            it('should have called onRemove', function(){
                expect(FilteringCollection.prototype.onRemove).toHaveBeenCalled();
            });
        });

        describe('After changing a model in the original collection', function() {
            beforeEach(function() {
                this.petCollection.findWhere({name: 'Rambo'}).set({animal: Animal.TIGER});
            });

            it('should update the model in the filtering collection', function(){
                expect(this.filteringCollection.findWhere({name: 'Rambo'}).get('animal')).toBe(Animal.TIGER);
            });

            it('should have called onChange', function(){
                expect(FilteringCollection.prototype.onChange).toHaveBeenCalled();
            });
        });

        describe('After resetting the original collection', function() {
            beforeEach(function() {
                this.petCollection.reset([{name: 'Luna', animal: Animal.TIGER}]);
            });

            it('should have called onReset', function(){
                expect(FilteringCollection.prototype.onReset).toHaveBeenCalled();
            });

            it('should update the filtering collection', function(){
                expect(this.filteringCollection.length).toBe(1);
            });

            it('should contain the new entry', function() {
                expect(this.filteringCollection.at(0)).toBe(this.petCollection.at(0));
            });
        });
    });
});
