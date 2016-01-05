define([
    'js-testing/backbone-mock-factory',
    'find/app/model/dates-filter-model',
    'find/hod/app/model/hod-search-filters-collection',
    'parametric-refinement/selected-values-collection',
    'databases-view/js/databases-collection',
    'find/app/model/backbone-query-model',
    'i18n!find/nls/bundle',
    'fieldtext/js/field-text-parser',
    'backbone',
    'moment'
], function (mockFactory, DatesFilterModel, FiltersCollection, SelectedParametricValues, DatabasesCollection, QueryModel, i18n, fieldTextParser, Backbone, moment) {

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

            this.queryModel.hasAnyChangedAttributes = function () {
                return true;
            };

            this.datesFilterModel = new Backbone.Model({
                dateRange: DatesFilterModel.dateRange.custom,
                minDate: INITIAL_MIN_DATE
            });

            this.selectedParametricValues = new SelectedParametricValues([
                {field: 'AGE', value: '4'}
            ]);

            this.collection = new FiltersCollection([], {
                queryModel: this.queryModel,
                datesFilterModel: this.datesFilterModel,
                indexesCollection: this.indexesCollection,
                selectedIndexesCollection: this.selectedIndexesCollection,
                selectedParametricValues: this.selectedParametricValues
            });
        });

        it('contains a databases filter model', function () {
            var model = this.collection.get(FiltersCollection.FilterTypes.indexes);
            expect(model).toBeDefined();
            expect(model.get('text')).toContain(WIKI_ENG.displayName);
            expect(model.get('text')).not.toContain(WOOKIEPEDIA.displayName);
        });
    });
});
