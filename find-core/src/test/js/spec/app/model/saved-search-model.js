/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'databases-view/js/databases-collection',
    'find/app/model/saved-searches/saved-search-model',
    'moment'
], function(Backbone, DatabasesCollection, SavedSearchModel, moment) {

    var INPUT_TEXT = 'johnny';
    var RELATED_CONCEPTS = ['depp'];
    var MAX_DATE = 555555555;
    var MIN_DATE = 444444444;

    var INDEXES = [
        {domain: 'DOMAIN', name: 'DOCUMENTS'}
    ];

    var PARAMETRIC_VALUES = [
        {field: 'CATEGORY', value: 'person'},
        {field: 'CATEGORY', value: 'film'}
    ];

    describe('SavedSearchModel', function() {
        beforeEach(function() {
            this.model = new SavedSearchModel({
                title: 'Johnny Depp',
                queryText: INPUT_TEXT,
                maxDate: moment(MAX_DATE),
                minDate: moment(MIN_DATE),
                relatedConcepts: RELATED_CONCEPTS,
                indexes: INDEXES,
                parametricValues: PARAMETRIC_VALUES
            });

            this.queryTextModel = new Backbone.Model({
                inputText: INPUT_TEXT,
                relatedConcepts: RELATED_CONCEPTS
            });

            this.queryModel = new Backbone.Model({
                maxDate: moment(MAX_DATE),
                minDate: moment(MIN_DATE)
            });

            this.selectedIndexes = new DatabasesCollection(INDEXES);

            // The real selected parametric values collection also contains display names
            this.selectedParametricValues = new Backbone.Collection(_.map(PARAMETRIC_VALUES, function(data, index) {
                return _.extend({
                    displayName: 'MY_DISPLAY_NAME_' + index
                }, data);
            }));

            this.queryState = {
                queryTextModel: this.queryTextModel,
                queryModel: this.queryModel,
                selectedIndexes: this.selectedIndexes,
                selectedParametricValues: this.selectedParametricValues
            };
        });

        describe('equalsQueryState', function() {
            it('returns true when the query state matches', function() {
                expect(this.model.equalsQueryState(this.queryState)).toBe(true);
            });

            it('returns false when the input text is different', function() {
                this.queryTextModel.set('inputText', 'cat');

                expect(this.model.equalsQueryState(this.queryState)).toBe(false);
            });

            it('returns false when the related concepts are different', function() {
                this.queryTextModel.set('relatedConcepts', ['pirate'].concat(RELATED_CONCEPTS));

                expect(this.model.equalsQueryState(this.queryState)).toBe(false);
            });

            it('returns false when one of the min dates is null', function() {
                this.queryModel.set('minDate', null);

                expect(this.model.equalsQueryState(this.queryState)).toBe(false);
            });

            it('returns false when the min dates are different', function() {
                this.queryModel.set('minDate', moment(123));

                expect(this.model.equalsQueryState(this.queryState)).toBe(false);
            });

            it('returns false when one of the max dates is null', function() {
                this.queryModel.set('maxDate', null);

                expect(this.model.equalsQueryState(this.queryState)).toBe(false);
            });

            it('returns false when the max dates are different', function() {
                this.queryModel.set('maxDate', moment(123));

                expect(this.model.equalsQueryState(this.queryState)).toBe(false);
            });

            it('returns false when the indexes are different', function() {
                this.selectedIndexes.add({domain: 'DOMAIN', name: 'MORE_DOCUMENTS'});

                expect(this.model.equalsQueryState(this.queryState)).toBe(false);
            });

            it('returns false when the related concepts are different', function() {
                this.selectedParametricValues.pop();

                expect(this.model.equalsQueryState(this.queryState)).toBe(false);
            });

            it('returns true if the only difference is an index domain being null rather than undefined', function() {
                var DATABASE_NAME = 'MORE_DOCUMENTS';
                this.selectedIndexes.add({domain: null, name: DATABASE_NAME});
                this.model.set('indexes', INDEXES.concat([{name: DATABASE_NAME}]));

                expect(this.model.equalsQueryState(this.queryState)).toBe(true);
            });
        });

        describe('attributesFromQueryState', function() {
            beforeEach(function() {
                this.attributes = SavedSearchModel.attributesFromQueryState(this.queryState);
            });

            it('returns the input text and related concepts from the query model', function() {
                expect(this.attributes.queryText).toBe(INPUT_TEXT);
                expect(this.attributes.relatedConcepts).toBe(RELATED_CONCEPTS);
            });

            it('returns the min and max dates from the query model', function() {
                expect(this.attributes.minDate.isSame(moment(MIN_DATE))).toBe(true);
                expect(this.attributes.maxDate.isSame(moment(MAX_DATE))).toBe(true);
            });

            it('returns the selected indexes', function() {
                expect(this.attributes.indexes).toEqual(INDEXES);
            });

            it('returns the selected parametric values', function() {
                expect(this.attributes.parametricValues).toEqual(PARAMETRIC_VALUES);
            });
        });
    });

});
