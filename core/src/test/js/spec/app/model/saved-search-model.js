/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/model/dates-filter-model',
    'find/app/model/min-score-model',
    'find/app/util/database-name-resolver',
    'moment'
], function(Backbone, _, SavedSearchModel, DatesFilterModel, MinScoreModel, databaseNameResolver, moment) {

    var INPUT_TEXT = 'johnny';
    var RELATED_CONCEPTS = [['depp']];
    var MAX_DATE = 555555555;
    var MIN_DATE = 444444444;
    var MIN_SCORE = 0;

    var BASE_INDEXES = [
        {domain: 'DOMAIN', name: 'DOCUMENTS'}
    ];

    var PARAMETRIC_VALUES = [
        {field: 'CATEGORY', value: 'person'},
        {field: 'CATEGORY', value: 'film'}
    ];

    var PARAMETRIC_RANGES_CLIENT = [
        {field: 'YEAR', range: [1066, 1485], dataType: 'numeric'},
        {field: 'DATE', range: [123456789000, 123456791000], dataType: 'date'}
    ];

    var PARAMETRIC_RANGES_SERVER = [
        {field: 'YEAR', min: 1066, max: 1485, type: 'Numeric'},
        {field: 'DATE', min: 123456789000, max: 123456791000, type: 'Date'}
    ];

    describe('SavedSearchModel', function() {
        beforeEach(function() {
            this.model = new SavedSearchModel({
                title: 'Johnny Depp',
                queryText: INPUT_TEXT,
                maxDate: moment(MAX_DATE),
                minDate: moment(MIN_DATE),
                minScore: MIN_SCORE,
                dateRange: DatesFilterModel.DateRange.CUSTOM,
                relatedConcepts: RELATED_CONCEPTS,
                indexes: BASE_INDEXES,
                parametricValues: PARAMETRIC_VALUES,
                parametricRanges: PARAMETRIC_RANGES_SERVER
            });

            this.queryTextModel = new Backbone.Model({
                inputText: INPUT_TEXT,
                relatedConcepts: RELATED_CONCEPTS
            });

            this.datesFilterModel = new DatesFilterModel({
                dateRange: DatesFilterModel.DateRange.CUSTOM,
                customMaxDate: moment(MAX_DATE),
                customMinDate: moment(MIN_DATE)
            });

            this.minScoreModel = new MinScoreModel({
                minScore: MIN_SCORE
            });

            this.selectedIndexes = new Backbone.Collection(BASE_INDEXES);
            databaseNameResolver.getDatabaseInfoFromCollection.and.callFake(function () {
                return BASE_INDEXES;
            });

            // The real selected parametric values collection also contains display names
            this.selectedParametricValues = new Backbone.Collection(_.map(PARAMETRIC_VALUES.concat(PARAMETRIC_RANGES_CLIENT), function(data, index) {
                return _.extend({
                    displayName: 'MY_DISPLAY_NAME_' + index
                }, data);
            }));

            this.queryState = {
                queryTextModel: this.queryTextModel,
                datesFilterModel: this.datesFilterModel,
                selectedIndexes: this.selectedIndexes,
                selectedParametricValues: this.selectedParametricValues,
                minScoreModel: this.minScoreModel
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
                this.queryTextModel.set('relatedConcepts', [['pirate']].concat(RELATED_CONCEPTS));

                expect(this.model.equalsQueryState(this.queryState)).toBe(false);
            });

            it('returns false when one of the min dates is null', function() {
                this.datesFilterModel.set('customMinDate', null);

                expect(this.model.equalsQueryState(this.queryState)).toBe(false);
            });

            it('returns false when the min dates are different', function() {
                this.datesFilterModel.set('customMinDate', moment(123));

                expect(this.model.equalsQueryState(this.queryState)).toBe(false);
            });

            it('returns false when one of the max dates is null', function() {
                this.datesFilterModel.set('customMaxDate', null);

                expect(this.model.equalsQueryState(this.queryState)).toBe(false);
            });

            it('returns false when the max dates are different', function() {
                this.datesFilterModel.set('customMaxDate', moment(123));

                expect(this.model.equalsQueryState(this.queryState)).toBe(false);
            });

            it('returns false when the indexes are different', function() {
                var newIndex = {domain: 'DOMAIN', name: 'MORE_DOCUMENTS'};
                this.selectedIndexes.add(newIndex);
                databaseNameResolver.getDatabaseInfoFromCollection.and.callFake(function () {
                    return [newIndex].concat(BASE_INDEXES);
                });

                expect(this.model.equalsQueryState(this.queryState)).toBe(false);
            });

            it('returns false when the related concepts are different', function() {
                this.selectedParametricValues.pop();

                expect(this.model.equalsQueryState(this.queryState)).toBe(false);
            });

            it('returns false if the min score is different', function() {
                this.minScoreModel.set('minScore', 45);

                expect(this.model.equalsQueryState(this.queryState)).toBe(false);
            })
        });

        describe('attributesFromQueryState', function() {
            beforeEach(function() {
                this.attributes = SavedSearchModel.attributesFromQueryState(this.queryState);
            });

            it('returns the input text and related concepts from the query text model', function() {
                expect(this.attributes.queryText).toBe(INPUT_TEXT);
                expect(this.attributes.relatedConcepts).toBe(RELATED_CONCEPTS);
            });

            it('returns the min and max dates from the dates filter model model', function() {
                expect(this.attributes.minDate.isSame(moment(MIN_DATE))).toBe(true);
                expect(this.attributes.maxDate.isSame(moment(MAX_DATE))).toBe(true);
            });

            it('returns the selected parametric values', function() {
                expect(this.attributes.parametricValues).toEqual(PARAMETRIC_VALUES);
            });

            it('returns the selected parametric ranges', function() {
                expect(this.attributes.parametricRanges).toEqual(PARAMETRIC_RANGES_SERVER);
            });

            it('returns the min score from the min score model', function() {
                expect(this.attributes.minScore).toBe(MIN_SCORE);
            });
        });
    });

});
