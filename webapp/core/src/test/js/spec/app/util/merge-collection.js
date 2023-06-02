/*
 * Copyright 2016-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'backbone',
    'find/app/util/merge-collection'
], function(Backbone, MergeCollection) {
    'use strict';

    const Animal = {CAT: 'CAT', DOG: 'DOG'};

    describe('Merge collection', function() {
        beforeEach(function() {
            this.catCollection = new Backbone.Collection([
                {id: 0, name: 'Oscar', animal: Animal.CAT},
                {id: 1, name: 'Katy', animal: Animal.CAT}
            ]);

            this.dogCollection = new Backbone.Collection([
                {id: 0, name: 'Rover', animal: Animal.DOG}
            ]);

            this.mergeCollection = new MergeCollection([], {
                collections: [this.catCollection, this.dogCollection],
                comparator: 'name',
                typeAttribute: 'animal'
            });
        });

        it('adds all existing models on construction', function() {
            expect(this.mergeCollection.length).toBe(3);
            expect(this.mergeCollection.at(0)).toBe(this.catCollection.at(1));
            expect(this.mergeCollection.at(1)).toBe(this.catCollection.at(0));
            expect(this.mergeCollection.at(2)).toBe(this.dogCollection.at(0));
        });

        it('adds models added to the tracked collection from itself', function() {
            const model = this.dogCollection.add({id: 1, name: 'Barky', animal: Animal.DOG});

            expect(this.mergeCollection.length).toBe(4);
            expect(this.mergeCollection.findWhere({name: 'Barky'})).toBe(model);
        });

        it('removes models removed from the tracked collections from itself', function() {
            this.catCollection.remove(0);

            expect(this.mergeCollection.length).toBe(2);
            expect(this.mergeCollection.findWhere({name: 'Oscar'})).toBeUndefined();
        });

        it('resets itself when one of the tracked collections is reset', function() {
            this.catCollection.reset([
                {id: 3, name: 'Tom', animal: Animal.CAT}
            ]);

            expect(this.mergeCollection.length).toBe(2);
            expect(this.mergeCollection.at(0)).toBe(this.dogCollection.at(0));
            expect(this.mergeCollection.at(1)).toBe(this.catCollection.at(0));
        });

        it('preserves the collection reference on the models', function() {
            expect(this.mergeCollection.findWhere({animal: Animal.CAT}).collection).toBe(this.catCollection);
        });

        it('handles adding two new models to the tracked collection', function() {
            // New models have no id
            this.catCollection.add({name: 'Willow', animal: Animal.CAT});
            this.catCollection.add({name: 'George', animal: Animal.CAT});

            expect(this.mergeCollection.length).toBe(5);
        });
    });
});
