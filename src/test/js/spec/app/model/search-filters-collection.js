define([
    'mock/backbone-mock-factory',
    'find/app/model/search-filters-collection',
    'find/app/model/backbone-query-model',
    'i18n!find/nls/bundle',
    'fieldtext/js/field-text-parser',
    'backbone',
    'moment'
], function(mockFactory, FiltersCollection, QueryModel, i18n, fieldTextParser, Backbone, moment) {

    var WOOKIEPEDIA = 'wookiepedia';
    var WIKI_ENG = 'wiki_eng';

    var INITIAL_MIN_DATE = moment();
    var DATE_FORMAT = 'LLL';

    describe('Search filters collection initialised with a min date and an indexes filter', function() {
        beforeEach(function() {
            this.queryModel = new Backbone.Model({
                minDate: INITIAL_MIN_DATE,
                indexes: [WIKI_ENG],
                allIndexesSelected: false
            });

            this.collection = new FiltersCollection([], {
                queryModel: this.queryModel
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
            expect(model.get('text')).toContain(WIKI_ENG);
            expect(model.get('text')).not.toContain(WOOKIEPEDIA);
        });

        describe('after a maxDate property is set on the request model', function() {
            beforeEach(function() {
                this.maxDate = moment(INITIAL_MIN_DATE).add(2, 'days');

                this.queryModel.set({
                    dateRange: QueryModel.DateRange.custom,
                    maxDate: this.maxDate
                })
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

        describe('after a new minDate is set on the request model', function() {
            beforeEach(function() {
                this.minDate = moment(INITIAL_MIN_DATE).subtract(2, 'days');

                this.queryModel.set({
                    dateRange: QueryModel.DateRange.custom,
                    minDate: this.minDate
                })
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
                this.allIndexesSelected = true;

                this.queryModel.set({
                    indexes: this.indexes,
                    allIndexesSelected: this.allIndexesSelected
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
                        indexes: [WOOKIEPEDIA],
                        allIndexesSelected: false
                    });
                });

                it('contains two models', function() {
                    expect(this.collection.length).toBe(2);
                });

                it('adds a databases filter model', function() {
                    var model = this.collection.get(FiltersCollection.FilterTypes.indexes);
                    expect(model).toBeDefined();
                    expect(model.get('text')).toContain(WOOKIEPEDIA);
                    expect(model.get('text')).not.toContain(WIKI_ENG);
                });
            });
        });

        describe('after the minDate model is removed', function() {
            beforeEach(function() {
                this.collection.remove(FiltersCollection.FilterTypes.minDate);
            });

            it('sets the request model minDate attribute to null', function() {
                expect(this.queryModel.get('minDate')).toBeNull();
            });
        });

        describe('after setParametricFieldText is called with a field text node containing restrictions on the AGE and NAME fields', function() {
            beforeEach(function() {
                var node = fieldTextParser.parse('MATCH{4}:AGE AND MATCH{bobby,penny}:NAME');
                this.collection.setParametricFieldText(node);
            });

            it('contains four models', function() {
                expect(this.collection.length).toBe(4);
            });

            it('contains an AGE parametric filter model', function() {
                var model = this.collection.findWhere({type: FiltersCollection.FilterTypes.PARAMETRIC, field: 'AGE'});
                expect(model).toBeDefined();
                expect(model.get('text')).toContain('AGE');
                expect(model.get('text')).toContain('4');
            });

            it('contains a NAME parametric filter model', function() {
                var model = this.collection.findWhere({type: FiltersCollection.FilterTypes.PARAMETRIC, field: 'NAME'});
                expect(model).toBeDefined();
                expect(model.get('text')).toContain('NAME');
                expect(model.get('text')).toContain('penny');
                expect(model.get('text')).toContain('bobby');
            });

            describe('then setParametricFieldText is called with a field text node containing a restriction on only the AGE field', function() {
                beforeEach(function() {
                    var node = fieldTextParser.parse('MATCH{6}:AGE');
                    this.collection.setParametricFieldText(node);
                });

                it('contains three models', function() {
                    expect(this.collection.length).toBe(3);
                });

                it('contains an AGE parametric filter model', function() {
                    var model = this.collection.findWhere({type: FiltersCollection.FilterTypes.PARAMETRIC, field: 'AGE'});
                    expect(model).toBeDefined();
                    expect(model.get('text')).toContain('AGE');
                    expect(model.get('text')).toContain('6');
                });
            });

            describe('then setParametricFieldText is called with a field text node containing a restriction on the GENDER field a different restriction on the NAME field', function() {
                beforeEach(function() {
                    var node = fieldTextParser.parse('MATCH{female}:GENDER AND MATCH{jo,jamie}:NAME');
                    this.collection.setParametricFieldText(node);
                });

                it('contains four models', function() {
                    expect(this.collection.length).toBe(4);
                });

                it('contains a GENDER parametric filter model', function() {
                    var model = this.collection.findWhere({type: FiltersCollection.FilterTypes.PARAMETRIC, field: 'GENDER'});
                    expect(model).toBeDefined();
                    expect(model.get('text')).toContain('GENDER');
                    expect(model.get('text')).toContain('female');
                });

                it('contains a NAME parametric filter model', function() {
                    var model = this.collection.findWhere({type: FiltersCollection.FilterTypes.PARAMETRIC, field: 'NAME'});
                    expect(model).toBeDefined();
                    expect(model.get('text')).toContain('NAME');
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
