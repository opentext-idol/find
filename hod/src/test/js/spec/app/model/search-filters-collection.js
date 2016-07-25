/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-testing/backbone-mock-factory',
    'find/app/model/dates-filter-model',
    'find/hod/app/model/hod-search-filters-collection',
    'parametric-refinement/selected-values-collection',
    'databases-view/js/databases-collection',
    'i18n!find/nls/bundle',
    'fieldtext/js/field-text-parser',
    'backbone',
    'moment'
], function (mockFactory, DatesFilterModel, FiltersCollection, SelectedParametricValues, DatabasesCollection, i18n, fieldTextParser, Backbone, moment) {

    var WOOKIEPEDIA = {
        id: 'TESTDOMAIN:wookiepedia',
        domain: 'TESTDOMAIN',
        name: 'wookiepedia',
        displayName: 'WookiePedia'
    };

    var WIKI_ENG = {
        id: 'TESTDOMAIN:wiki_eng',
        domain: 'TESTDOMAIN',
        name: 'wiki_eng',
        displayName: 'Wikipedia (Eng)'
    };

    var INITIAL_MIN_DATE = moment();

    describe('Search filters collection initialised with an indexes filter, a DatesFilterModel with a min date set and a selected parametric value on the AGE field', function () {
        beforeEach(function () {
            this.indexesCollection = new DatabasesCollection([WOOKIEPEDIA, WIKI_ENG]);
            this.selectedIndexesCollection = new DatabasesCollection([WIKI_ENG]);

            this.queryModel = new Backbone.Model({
                minDate: INITIAL_MIN_DATE
            });

            this.datesFilterModel = new Backbone.Model({
                dateRange: DatesFilterModel.DateRange.CUSTOM,
                customMinDate: INITIAL_MIN_DATE
            });

            this.selectedParametricValues = new SelectedParametricValues([
                {field: 'AGE', value: '4'}
            ]);

            this.collection = new FiltersCollection([], {
                queryModel: this.queryModel,
                indexesCollection: this.indexesCollection,
                queryState: {
                    datesFilterModel: this.datesFilterModel,
                    selectedIndexes: this.selectedIndexesCollection,
                    selectedParametricValues: this.selectedParametricValues
                }
            });
        });

        it('contains a databases filter model', function () {
            var model = this.collection.get(FiltersCollection.FilterType.INDEXES);
            expect(model).toBeDefined();
            expect(model.get('text')).toContain(WIKI_ENG.displayName);
            expect(model.get('text')).not.toContain(WOOKIEPEDIA.displayName);
        });
    });
});
