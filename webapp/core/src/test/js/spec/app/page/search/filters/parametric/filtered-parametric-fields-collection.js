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
    'find/app/page/search/filters/parametric/filtered-parametric-fields-collection'
], function(_, Backbone, FilteredParametricFieldsCollection) {
    'use strict';

    const Animal = {CAT: 'CAT', DOG: 'DOG', TIGER: 'TIGER', UNICORN: 'UNICORN'};

    const MockCollection = Backbone.Collection.extend({
        spy1: function() {
        },
        spy2: function() {
        },
        spy3: function() {
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
                {id: 'Rambo', displayName: 'Rambo', type: 'Parametric'},
                {id: 'Millie', displayName: 'Millie', type: 'Parametric'},
                {id: 'Maisey', displayName: 'Maisey', type: 'Parametric'},
                {id: 'Hobbes', displayName: 'Hobbes', type: 'Parametric'}
            ]);

            this.parametricCollection = new Backbone.Collection([
                {
                    id: 'Rambo',
                    displayName: 'Rambo',
                    totalValues: 1,
                    values: [{value: Animal.CAT, displayValue: Animal.CAT}],
                    type: 'Parametric'
                },
                {
                    id: 'Millie',
                    displayName: 'Millie',
                    totalValues: 1,
                    values: [{value: Animal.CAT, displayValue: Animal.CAT}],
                    type: 'Parametric'
                },
                {
                    id: 'Maisey',
                    displayName: 'Maisey',
                    totalValues: 1,
                    values: [{value: Animal.DOG, displayValue: Animal.DOG}],
                    type: 'Parametric'
                },
                {
                    id: 'Hobbes',
                    displayName: 'Hobbes',
                    totalValues: 1,
                    values: [{value: Animal.TIGER, displayValue: Animal.TIGER}],
                    type: 'Parametric'
                }
            ]);

            this.filteredParametricCollection = new Backbone.Collection(this.parametricCollection.models);

            this.filterModel = new Backbone.Model({text: ''});

            spyOn(this.parametricFieldsCollection, 'spy1').and.callThrough();
            spyOn(this.parametricFieldsCollection, 'spy2').and.callThrough();
            spyOn(this.parametricFieldsCollection, 'spy3').and.callThrough();

            const queryModel = new Backbone.Model();
            queryModel.getIsoDate = _.noop;
            this.filteredParametricFieldsCollection = new FilteredParametricFieldsCollection([], {
                queryModel: queryModel,
                parametricCollection: this.parametricCollection,
                filteredParametricCollection: this.filteredParametricCollection,
                collection: this.parametricFieldsCollection,
                filterModel: this.filterModel,
                collectionFunctions: ['spy1', 'spy2', 'spy3']
            });
        });

        it('should have 4 elements in the filtered collection', function() {
            expect(this.filteredParametricFieldsCollection.length).toEqual(4);
        });

        describe('after filtering by the letter "M"', function() {
            beforeEach(function() {
                this.filterModel.set({text: 'm'});
                this.filteredParametricFieldsCollection.valueRestrictedParametricCollection.trigger('sync');
            });

            it('should show 3 models when the filter text is set to "m"', function() {
                expect(this.filteredParametricFieldsCollection.length).toEqual(3);
                expect(this.filteredParametricFieldsCollection.at(0))
                    .toBe(this.parametricFieldsCollection.at(0));
                expect(this.filteredParametricFieldsCollection.at(1))
                    .toBe(this.parametricFieldsCollection.at(1));
                expect(this.filteredParametricFieldsCollection.at(2))
                    .toBe(this.parametricFieldsCollection.at(2));
            });

            it('should set 3 parametric models', function() {
                expect(this.filteredParametricCollection.length).toEqual(3);
            });
        });

        describe('after filtering by the letter "O"', function() {
            beforeEach(function() {
                this.filterModel.set({text: 'o'});
                this.filteredParametricFieldsCollection.valueRestrictedParametricCollection.set([
                    {
                        id: 'Maisey',
                        displayName: 'Maisey',
                        totalValues: 1,
                        values: [{value: Animal.DOG, displayValue: Animal.DOG}],
                        type: 'Parametric'
                    }
                ]);
                this.filteredParametricFieldsCollection.valueRestrictedParametricCollection.trigger('sync');
            });

            it('should show 3 models when the filter text is set to "o"', function() {
                expect(this.filteredParametricFieldsCollection.length).toEqual(3);
                expect(this.filteredParametricFieldsCollection.at(0))
                    .toBe(this.parametricFieldsCollection.at(0));
                expect(this.filteredParametricFieldsCollection.at(1))
                    .toBe(this.parametricFieldsCollection.at(2));
                expect(this.filteredParametricFieldsCollection.at(2))
                    .toBe(this.parametricFieldsCollection.at(3));
            });

            it('should set 3 parametric models', function() {
                expect(this.filteredParametricCollection.length).toEqual(3);
            });

            describe('after parametric collection update', function() {
                beforeEach(function() {
                    this.parametricCollection.set([
                        {
                            id: 'Rambo',
                            displayName: 'Rambo',
                            totalValues: 1,
                            values: [{value: Animal.CAT, displayValue: Animal.CAT}],
                            type: 'Parametric'
                        },
                        {
                            id: 'Hobbes',
                            displayName: 'Hobbes',
                            totalValues: 1,
                            values: [{value: Animal.TIGER, displayValue: Animal.TIGER}],
                            type: 'Parametric'
                        }
                    ]);
                    this.parametricCollection.trigger('sync');
                    this.filteredParametricFieldsCollection
                        .valueRestrictedParametricCollection.set([]);
                    this.filteredParametricFieldsCollection
                        .valueRestrictedParametricCollection.trigger('sync');
                });

                it('should show 2 models', function() {
                    expect(this.filteredParametricFieldsCollection.length).toEqual(2);
                    expect(this.filteredParametricFieldsCollection.at(0))
                        .toBe(this.parametricFieldsCollection.at(0));
                    expect(this.filteredParametricFieldsCollection.at(1))
                        .toBe(this.parametricFieldsCollection.at(3));
                });

                it('should set 2 parametric models', function() {
                    expect(this.filteredParametricCollection.length).toEqual(2);
                });
            });
        });

        describe('after filtering by the letters "Hobbes"', function() {
            beforeEach(function() {
                this.filterModel.set({text: 'Hobbes'});
                this.filteredParametricFieldsCollection.valueRestrictedParametricCollection.trigger('sync');
            });

            it('should show 1 model when the filter text is set to "Hobbes"', function() {
                expect(this.filteredParametricFieldsCollection.length).toEqual(1);
                expect(this.filteredParametricFieldsCollection.at(0))
                    .toBe(this.parametricFieldsCollection.at(3));
            });

            it('should set 1 parametric model', function() {
                expect(this.filteredParametricCollection.length).toEqual(1);
            });
        });

        describe('When calling the collection functions', function() {
            beforeEach(function() {
                this.filteredParametricFieldsCollection.spy1();
                this.filteredParametricFieldsCollection.spy2(1, 2, 3);
            });

            it('should have called the original functions', function() {
                expect(this.parametricFieldsCollection.spy1)
                    .toHaveBeenCalled();
                expect(this.parametricFieldsCollection.spy2)
                    .toHaveBeenCalled();
                expect(this.parametricFieldsCollection.spy3)
                    .not.toHaveBeenCalled();

                expect(this.parametricFieldsCollection.spy2.calls.argsFor(0)).toEqual([1, 2, 3]);
            })
        });

        describe('After adding a model to the original collection', function() {
            beforeEach(function() {
                this.parametricFieldsCollection.add(new Backbone.Model({
                    id: 'Twilight',
                    displayName: 'Twilight',
                    type: 'Parametric'
                }));
            });

            it('should have 5 models', function() {
                expect(this.filteredParametricFieldsCollection.length).toEqual(5);
            });

            it('should have called onAdd', function() {
                expect(FilteredParametricFieldsCollection.prototype.onAdd)
                    .toHaveBeenCalled();
            });
        });

        describe('After removing a model from the original collection', function() {
            beforeEach(function() {
                this.parametricFieldsCollection.remove(this.parametricFieldsCollection.at(0));
            });

            it('filtering collection should have 3 models', function() {
                expect(this.filteredParametricFieldsCollection.length).toEqual(3);
            });

            it('should have called onRemove', function() {
                expect(FilteredParametricFieldsCollection.prototype.onRemove)
                    .toHaveBeenCalled();
            });
        });

        describe('After changing a model in the original collection', function() {
            beforeEach(function() {
                this.parametricFieldsCollection.findWhere({displayName: 'Rambo'}).set({displayName: 'Rocky'});
            });

            it('should update the model in the filtering collection', function() {
                expect(this.filteredParametricFieldsCollection.findWhere({id: 'Rambo'}).get('displayName'))
                    .toEqual('Rocky');
            });

            it('should have called onChange', function() {
                expect(FilteredParametricFieldsCollection.prototype.onChange)
                    .toHaveBeenCalled();
            });
        });

        describe('After resetting the original collection', function() {
            beforeEach(function() {
                this.parametricFieldsCollection.reset([{
                    id: 'Luna',
                    displayName: 'Luna',
                    type: 'Parametric'
                }]);
            });

            it('should have called onReset', function() {
                expect(FilteredParametricFieldsCollection.prototype.onReset)
                    .toHaveBeenCalled();
            });

            it('should update the filtering collection', function() {
                expect(this.filteredParametricFieldsCollection.length).toEqual(1);
            });

            it('should contain the new entry', function() {
                expect(this.filteredParametricFieldsCollection.at(0))
                    .toBe(this.parametricFieldsCollection.at(0));
            });
        });
    });
});
