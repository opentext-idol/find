define([
    'mock/backbone-mock-factory',
    'find/app/model/dates-filter-model',
    'find/app/model/search-filters-collection',
    'mock/model/indexes-collection',
    'find/app/model/backbone-query-model',
    'i18n!find/nls/bundle',
    'fieldtext/js/field-text-parser',
    'backbone',
    'moment'
], function(mockFactory, DatesFilterModel, FiltersCollection, IndexesCollection, QueryModel, i18n, fieldTextParser, Backbone, moment) {

    var WOOKIEPEDIA = {
        id: 'TESTDOMAIN:wookiepedia',
        domain: 'TESTDOMAIN',
        name: 'wookiepedia'
    };
    var WIKI_ENG = {
        id: 'TESTDOMAIN:wiki_eng',
        domain: 'TESTDOMAIN',
        name: 'wiki_eng'
    };

    var INITIAL_MIN_DATE = moment();
    var DATE_FORMAT = 'LLL';

    describe('Search filters collection initialised with an indexes filter and DatesFilterModel with a min date set', function() {
        beforeEach(function() {
            IndexesCollection.reset();

            this.indexesCollection = new IndexesCollection();

            this.indexesCollection.set([
                WOOKIEPEDIA,
                WIKI_ENG
            ]);

            this.queryModel = new Backbone.Model({
                indexes: [WIKI_ENG.id],
                minDate: INITIAL_MIN_DATE
            });

            this.datesFilterModel = new Backbone.Model({
                dateRange: DatesFilterModel.dateRange.custom,
                minDate: INITIAL_MIN_DATE
            });

            this.collection = new FiltersCollection([], {
                queryModel: this.queryModel,
                datesFilterModel: this.datesFilterModel,
                indexesCollection: this.indexesCollection
            });
        });

        it('contains two models', function() {
            expect(this.collection.length).toBe(2);
        });

        it('contains a min date filter model', function() {
            var model = this.collection.get(FiltersCollection.FilterTypes.minDate);
            expect(model).toBeDefined();
            expect(model.get('text')).toContain(INITIAL_MIN_DATE.format(DATE_FORMAT));
        });

        it('contains a databases filter model', function() {
            var model = this.collection.get(FiltersCollection.FilterTypes.indexes);
            expect(model).toBeDefined();
            expect(model.get('text')).toContain(WIKI_ENG.name);
            expect(model.get('text')).not.toContain(WOOKIEPEDIA.name);
        });

        describe('after datesFilterModel has a maxDate set', function() {
            beforeEach(function() {
                this.maxDate = moment(INITIAL_MIN_DATE).add(2, 'days');

                this.queryModel.set('maxDate', this.maxDate);

                this.datesFilterModel.set({
                    dateRange: DatesFilterModel.dateRange.custom,
                    maxDate: this.maxDate
                });
            });

            it('contains three models', function() {
                expect(this.collection.length).toBe(3);
            });

            it('contains a max date filter model with the correct date', function() {
                var model = this.collection.get(FiltersCollection.FilterTypes.maxDate);
                expect(model).toBeDefined();
                expect(model.get('text')).toContain(i18n['app.until']);
                expect(model.get('text')).toContain(moment(this.maxDate).format(DATE_FORMAT));
            });
        });

        describe('after datesFilterModel has a minDate set', function() {
            beforeEach(function() {
                this.minDate = moment(INITIAL_MIN_DATE).subtract(2, 'days');

                this.datesFilterModel.set({
                    dateRange: DatesFilterModel.dateRange.custom,
                    minDate: this.minDate
                });

                this.queryModel.set('minDate', this.minDate);
            });

            it('contains two models', function() {
                expect(this.collection.length).toBe(2);
            });

            it('updates the min date filter model', function() {
                var model = this.collection.get(FiltersCollection.FilterTypes.minDate);
                expect(model.get('text')).toContain(i18n['app.from']);
                expect(model.get('text')).toContain(moment(this.minDate).format(DATE_FORMAT))
            });
        });

        describe('after all databases are selected', function() {
            beforeEach(function() {
                this.indexes = [WOOKIEPEDIA, WIKI_ENG];

                this.queryModel.set({
                    indexes: this.indexes
                });
            });

            it('contains one model', function() {
                expect(this.collection.length).toBe(1);
            });

            it('removes the databases filter model', function() {
                expect(this.collection.get(FiltersCollection.FilterTypes.indexes)).toBeUndefined();
            });

            describe('then a database is deselected', function() {
                beforeEach(function() {
                    this.queryModel.set({
                        indexes: [WOOKIEPEDIA.id]
                    });
                });

                it('contains two models', function() {
                    expect(this.collection.length).toBe(2);
                });

                it('adds a databases filter model', function() {
                    var model = this.collection.get(FiltersCollection.FilterTypes.indexes);
                    expect(model).toBeDefined();
                    expect(model.get('text')).toContain(WOOKIEPEDIA.name);
                    expect(model.get('text')).not.toContain(WIKI_ENG.name);
                });
            });
        });

        describe('after datesFilterModel has a minDate set', function() {
            beforeEach(function() {
                this.datesFilterModel.set({
                    dateRange: null,
                    minDate: null
                });

                this.queryModel.set('minDate', null);
            });

            it('sets the request model minDate attribute to null', function() {
                expect(this.queryModel.get('minDate')).toBeNull();
            });
        });

        describe('after setParametricFieldText is called with a field text node containing restrictions on the AGE and NAME fields', function() {
            beforeEach(function() {
                var node = fieldTextParser.parse('MATCH{4}:AGE AND MATCH{bobby,penny}:NAME');
                node.left.displayField = 'Age';
                node.right.displayField = 'Name';

                this.collection.setParametricFieldText(node);
            });

            it('contains four models', function() {
                expect(this.collection.length).toBe(4);
            });

            it('contains an AGE parametric filter model', function() {
                var model = this.collection.findWhere({type: FiltersCollection.FilterTypes.PARAMETRIC, field: 'AGE'});
                expect(model).toBeDefined();
                expect(model.get('text')).toContain('Age');
                expect(model.get('text')).toContain('4');
            });

            it('contains a NAME parametric filter model', function() {
                var model = this.collection.findWhere({type: FiltersCollection.FilterTypes.PARAMETRIC, field: 'NAME'});
                expect(model).toBeDefined();
                expect(model.get('text')).toContain('Name');
                expect(model.get('text')).toContain('penny');
                expect(model.get('text')).toContain('bobby');
            });

            describe('then setParametricFieldText is called with a field text node containing a restriction on only the AGE field', function() {
                beforeEach(function() {
                    var node = fieldTextParser.parse('MATCH{6}:AGE');
                    node.displayField = 'Age';
                    this.collection.setParametricFieldText(node);
                });

                it('contains three models', function() {
                    expect(this.collection.length).toBe(3);
                });

                it('contains an AGE parametric filter model', function() {
                    var model = this.collection.findWhere({type: FiltersCollection.FilterTypes.PARAMETRIC, field: 'AGE'});
                    expect(model).toBeDefined();
                    expect(model.get('text')).toContain('Age');
                    expect(model.get('text')).toContain('6');
                });
            });

            describe('then setParametricFieldText is called with a field text node containing a restriction on the GENDER field a different restriction on the NAME field', function() {
                beforeEach(function() {
                    var node = fieldTextParser.parse('MATCH{female}:GENDER AND MATCH{jo,jamie}:NAME');
                    node.left.displayField = 'Gender';
                    node.right.displayField = 'Name';
                    this.collection.setParametricFieldText(node);
                });

                it('contains four models', function() {
                    expect(this.collection.length).toBe(4);
                });

                it('contains a GENDER parametric filter model', function() {
                    var model = this.collection.findWhere({type: FiltersCollection.FilterTypes.PARAMETRIC, field: 'GENDER'});
                    expect(model).toBeDefined();
                    expect(model.get('text')).toContain('Gender');
                    expect(model.get('text')).toContain('female');
                });

                it('contains a NAME parametric filter model', function() {
                    var model = this.collection.findWhere({type: FiltersCollection.FilterTypes.PARAMETRIC, field: 'NAME'});
                    expect(model).toBeDefined();
                    expect(model.get('text')).toContain('Name');
                    expect(model.get('text')).toContain('jamie');
                    expect(model.get('text')).toContain('jo');
                });
            });

            describe('then setParametricFieldText is called with null', function() {
                beforeEach(function() {
                    this.collection.setParametricFieldText(null);
                });

                it('contains two models', function() {
                    expect(this.collection.length).toBe(2);
                });

                it('contains no parametric filter models', function() {
                    expect(this.collection.findWhere({type: FiltersCollection.FilterTypes.PARAMETRIC})).toBeUndefined();
                });
            });
        });
    });

});
