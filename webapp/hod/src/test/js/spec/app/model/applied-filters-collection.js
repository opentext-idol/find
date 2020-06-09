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
    'backbone',
    'moment',
    'js-testing/backbone-mock-factory',
    'find/app/model/dates-filter-model',
    'find/app/model/geography-model',
    'find/app/model/applied-filters-collection',
    'parametric-refinement/selected-values-collection',
    'databases-view/js/hod-databases-collection',
    'i18n!find/nls/bundle',
    'fieldtext/js/field-text-parser',
    'find/app/configuration'
], function(Backbone, moment, mockFactory, DatesFilterModel, GeographyModel, FiltersCollection,
            SelectedParametricValues, DatabasesCollection, i18n, fieldTextParser, configuration) {
    'use strict';

    const WOOKIEPEDIA = {
        id: 'TESTDOMAIN:wookiepedia',
        domain: 'TESTDOMAIN',
        name: 'wookiepedia',
        displayName: 'WookiePedia'
    };

    const WIKI_ENG = {
        id: 'TESTDOMAIN:wiki_eng',
        domain: 'TESTDOMAIN',
        name: 'wiki_eng',
        displayName: 'Wikipedia (Eng)'
    };

    const INITIAL_MIN_DATE = moment();

    describe('Search filters collection initialised with an indexes filter, a DatesFilterModel with a min date set and a selected parametric value on the AGE field', function() {
        beforeEach(function() {
            this.indexesCollection = new DatabasesCollection([WOOKIEPEDIA, WIKI_ENG]);
            this.selectedIndexesCollection = new DatabasesCollection([WIKI_ENG]);

            this.queryModel = new Backbone.Model({
                minDate: INITIAL_MIN_DATE
            });

            this.datesFilterModel = new Backbone.Model({
                dateRange: DatesFilterModel.DateRange.CUSTOM,
                customMinDate: INITIAL_MIN_DATE
            });

            this.geographyModel = new GeographyModel({})

            this.selectedParametricValues = new SelectedParametricValues([
                {field: 'AGE', displayName: 'Age', value: '4', displayValue: '4', type: 'Parametric'}
            ]);

            this.collection = new FiltersCollection([], {
                queryModel: this.queryModel,
                indexesCollection: this.indexesCollection,
                queryState: {
                    datesFilterModel: this.datesFilterModel,
                    geographyModel: this.geographyModel,
                    selectedIndexes: this.selectedIndexesCollection,
                    selectedParametricValues: this.selectedParametricValues
                }
            });
        });

        it('contains a databases filter model', function() {
            const model = this.collection.get(FiltersCollection.FilterType.INDEXES);
            expect(model).toBeDefined();
            expect(model.get('text')).toContain(WIKI_ENG.displayName);
            expect(model.get('text')).not.toContain(WOOKIEPEDIA.displayName);
        });
    });
});
