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
    'backbone',
    'underscore',
    'find/app/configuration',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/model/dates-filter-model',
    'find/app/model/geography-model',
    'find/app/model/document-selection-model',
    'find/app/model/min-score-model',
    'find/app/util/database-name-resolver',
    'moment'
], function(Backbone, _, configuration, SavedSearchModel, DatesFilterModel, GeographyModel, DocumentSelectionModel, MinScoreModel, databaseNameResolver, moment) {

    const configWithTwoFields = {
        map: {
            "enabled" : true,
            "locationFields" : [
                {
                    "id": "DefaultLocation",
                    "displayName": "Default Location",
                    "latitudeField": "latitude",
                    "longitudeField": "longitude",
                    "iconName": null,
                    "iconColor": null,
                    "markerColor": null
                },
                {
                    "id": "OGLocation",
                    "displayName": "OG Location",
                    "latitudeField": "oglatitude",
                    "longitudeField": "oglongitude",
                    "iconName": "hp-pin",
                    "iconColor": "blue",
                    "markerColor": "orange"
                }
            ]
        },
        fieldsInfo: {
            "latitude": {
                "names": [
                    "NODE_PLACE/LAT",
                    "LAT"
                ],
                "type": "NUMBER",
                "advanced": true
            },
            "longitude": {
                "names": [
                    "NODE_PLACE/LON",
                    "LON"
                ],
                "type": "NUMBER",
                "advanced": true
            },
            "oglatitude": {
                "names": [
                    "OG_LATITUDE"
                ],
                "type": "NUMBER",
                "advanced": true
            },
            "oglongitude": {
                "names": [
                    "OG_LONGITUDE"
                ],
                "type": "NUMBER",
                "advanced": true
            }
        }
    };

    const RELATED_CONCEPTS = [['johnny'], ['depp']];
    const MAX_DATE = 555555555;
    const MIN_DATE = 444444444;
    const MIN_SCORE = 0;

    const BASE_INDEXES = [
        {domain: 'DOMAIN', name: 'DOCUMENTS'}
    ];

    const GEOGRAPHY_FILTERS = [
        { field: 'DefaultLocation', json: '{"type":"circle","center":[-7.013,-193.007],"radius":3511716.726}' },
        { field: 'OGLocation', json: '{"type":"circle","center":[40.123,60.321],"radius":123456.1}' },
        { field: 'OGLocation', json: '{"type":"polygon","points":[[-12.76,-206.71],[-5.09,-170.51],[-27.21,-168.75],[-29.07,-200.12]]}' }
    ];

    const DOCUMENT_SELECTION = [
        { reference: 'a' },
        { reference: 'b' },
        { reference: 'c' }
    ];

    const PARAMETRIC_VALUES = [
        {field: 'CATEGORY', displayName: 'Category', value: 'person', displayValue: 'Person'},
        {field: 'CATEGORY', displayName: 'Category', value: 'film', displayValue: 'Film'}
    ];

    const PARAMETRIC_RANGES_CLIENT = [
        {field: 'YEAR', displayName: 'Year', range: [1066, 1485], type: 'Numeric'},
        {field: 'DATE', displayName: 'Date', range: [123456789000, 123456791000], type: 'NumericDate'}
    ];

    const PARAMETRIC_RANGES_SERVER = [
        {field: 'YEAR', displayName: 'Year', min: 1066, max: 1485, type: 'Numeric'},
        {field: 'DATE', displayName: 'Date', min: 123456789000, max: 123456791000, type: 'Date'}
    ];

    describe('SavedSearchModel', function() {
        afterEach(function(){
            GeographyModel.parseConfiguration(configuration());
        })

        beforeEach(function() {
            GeographyModel.parseConfiguration(configWithTwoFields)

            this.model = new SavedSearchModel({
                title: 'Johnny Depp',
                maxDate: moment(MAX_DATE),
                minDate: moment(MIN_DATE),
                minScore: MIN_SCORE,
                dateRange: DatesFilterModel.DateRange.CUSTOM,
                geographyFilters: GEOGRAPHY_FILTERS,
                documentSelectionIsWhitelist: false,
                documentSelection: DOCUMENT_SELECTION,
                relatedConcepts: RELATED_CONCEPTS,
                indexes: BASE_INDEXES,
                parametricValues: PARAMETRIC_VALUES,
                parametricRanges: PARAMETRIC_RANGES_SERVER
            });

            this.conceptGroups = new Backbone.Collection(RELATED_CONCEPTS.map(function(concepts) {
                return {concepts: concepts};
            }));

            this.datesFilterModel = new DatesFilterModel({
                dateRange: DatesFilterModel.DateRange.CUSTOM,
                customMaxDate: moment(MAX_DATE),
                customMinDate: moment(MIN_DATE)
            });

            this.geographyModel = new GeographyModel({
                'DefaultLocation': [
                    {"type":"circle","center":[-7.013,-193.007],"radius":3511716.726},
                ],
                'OGLocation': [
                    {"type":"circle","center":[40.123,60.321],"radius":123456.1},
                    {"type":"polygon","points":[[-12.76,-206.71],[-5.09,-170.51],[-27.21,-168.75],[-29.07,-200.12]]}
                ]
            });

            this.documentSelectionModel = new DocumentSelectionModel({
                isWhitelist: false,
                references: ['a', 'b', 'c']
            });

            this.minScoreModel = new MinScoreModel({
                minScore: MIN_SCORE
            });

            this.selectedIndexes = new Backbone.Collection(BASE_INDEXES);
            databaseNameResolver.getDatabaseInfoFromCollection.and.callFake(function () {
                return BASE_INDEXES;
            });

            // The real selected parametric values collection also contains display names
            this.selectedParametricValues = new Backbone.Collection(PARAMETRIC_VALUES.concat(PARAMETRIC_RANGES_CLIENT));

            this.queryState = {
                conceptGroups: this.conceptGroups,
                datesFilterModel: this.datesFilterModel,
                geographyModel: this.geographyModel,
                documentSelectionModel: this.documentSelectionModel,
                selectedIndexes: this.selectedIndexes,
                selectedParametricValues: this.selectedParametricValues,
                minScoreModel: this.minScoreModel
            };
        });

        describe('equalsQueryState', function() {
            it('returns true when the query state matches', function() {
                expect(this.model.equalsQueryState(this.queryState)).toBe(true);
            });

            it('returns false when the related concepts are different', function() {
                this.conceptGroups.unshift({concepts: ['pirate']});

                expect(this.model.equalsQueryState(this.queryState)).toBe(false);
            });

            it('returns false when the geography model is different', function() {
                this.geographyModel.set('OGLocation', [])

                expect(this.model.equalsQueryState(this.queryState)).toBe(false);
            });

            it('returns false when the document selection model is different', function() {
                this.documentSelectionModel.exclude('d');

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
                const newIndex = {domain: 'DOMAIN', name: 'MORE_DOCUMENTS'};
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
            });
        });

        describe('attributesFromQueryState', function() {
            beforeEach(function() {
                this.attributes = SavedSearchModel.attributesFromQueryState(this.queryState);
            });

            it('returns the related concepts from the concept groups collection', function() {
                expect(this.attributes.relatedConcepts).toEqual(RELATED_CONCEPTS);
            });

            it('returns the geographic filters from the geographic model', function() {
                expect(this.attributes.geographyFilters).toEqual(GEOGRAPHY_FILTERS);
            });

            it('returns the document selection information', function() {
                expect(this.attributes.documentSelectionIsWhitelist).toEqual(false);
                expect(this.attributes.documentSelection).toEqual(DOCUMENT_SELECTION);
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

        describe('toConceptGroups', function() {
            it('maps the relatedConcepts to concept group model attributes', function() {
                expect(this.model.toConceptGroups()).toEqual([
                    {concepts: RELATED_CONCEPTS[0]},
                    {concepts: RELATED_CONCEPTS[1]}
                ]);
            });
        });
    });

});
