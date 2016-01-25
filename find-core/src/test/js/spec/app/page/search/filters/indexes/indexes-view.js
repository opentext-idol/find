define([
    'backbone',
    'underscore',
    'jquery',
    'find/app/page/search/filters/indexes/indexes-view',
    'databases-view/js/databases-collection'
], function(Backbone, _, $, IndexesView, DatabasesCollection) {

    describe('Indexes View', function() {
        var DOMAIN = 'TEST';

        var indexId =  function(name) {
            return DOMAIN + ':' + name;
        };

        var INDEXES = _.map(['a','b','c'], function(name) {
            return {name: name, domain: DOMAIN, id: indexId(name)};
        });

        // Convert index collection model attributes to a resource identifier object
        var toResourceIdentifier = _.partial(_.pick, _, 'domain', 'name');

        beforeEach(function() {
            this.indexesCollection = new DatabasesCollection();
            this.selectedIndexesCollection = new DatabasesCollection();
            this.queryModel = new Backbone.Model();

            this.idElement = function(indexAttributes) {
                return this.indexesView.$('li[data-id="' + indexAttributes.id + '"]');
            };
        });

        describe('initialized with a populated indexes collection', function() {
            beforeEach(function() {
                this.indexesCollection.reset(INDEXES);
                this.queryModel.set('indexes', _.pluck(INDEXES, 'id'));

                this.indexesView = new IndexesView({
                    queryModel: this.queryModel,
                    indexesCollection: this.indexesCollection,
                    selectedDatabasesCollection: this.selectedIndexesCollection
                });

                this.indexesView.render();
            });

            it('should display indexes in the IndexesCollection', function() {
                var elements = this.indexesView.$el.find('[data-id]');

                var dataIds = _.map(elements, function (element) {
                    return $(element).attr('data-id');
                });

                expect(dataIds).toContain(INDEXES[0].id);
                expect(dataIds).toContain(INDEXES[1].id);
                expect(dataIds).toContain(INDEXES[2].id);
            });
        });

        describe('initialized with an empty indexes collection which is then reset', function() {
            beforeEach(function() {
                this.indexesView = new IndexesView({
                    queryModel: this.queryModel,
                    indexesCollection: this.indexesCollection,
                    selectedDatabasesCollection: this.selectedIndexesCollection
                });

                this.indexesView.render();

                this.indexesCollection.reset(INDEXES);
                this.queryModel.set('indexes', _.pluck(INDEXES, 'id'));
            });

            it('should display indexes in the IndexesCollection', function() {
                var elements = this.indexesView.$el.find('[data-id]');

                var dataIds = _.map(elements, function (element) {
                    return $(element).attr('data-id');
                });

                expect(dataIds).toContain(INDEXES[0].id);
                expect(dataIds).toContain(INDEXES[1].id);
                expect(dataIds).toContain(INDEXES[2].id);
            });

            it('sets all indexes on the selected indexes collection', function() {
                expect(this.selectedIndexesCollection.length).toBe(3);
            });

            it('does not select indexes in the UI', function() {
                expect(this.indexesView.$('i.hp-icon hp-check')).toHaveLength(0);
            });

            describe('clicking an index once', function() {
                beforeEach(function() {
                    this.idElement(INDEXES[0]).click();
                });

                it('updates the selected indexes collection', function() {
                    expect(this.selectedIndexesCollection.toResourceIdentifiers()).toEqual([toResourceIdentifier(INDEXES[0])]);
                });

                it('should check the clicked index', function() {
                    var checkedCheckbox = this.idElement(INDEXES[0]).find('i');
                    var uncheckedCheckboxOne = this.idElement(INDEXES[1]).find('i');
                    var uncheckedCheckboxTwo = this.idElement(INDEXES[2]).find('i');

                    expect(checkedCheckbox).toHaveClass('hp-check');
                    expect(uncheckedCheckboxOne).not.toHaveClass('hp-check');
                    expect(uncheckedCheckboxTwo).not.toHaveClass('hp-check');
                });
            });

            describe('clicking an index twice', function() {
                beforeEach(function() {
                    this.idElement(INDEXES[0]).click().click();
                });

                it('updates the selected indexes collection with all of the indexes', function() {
                    expect(this.selectedIndexesCollection.length).toBe(3);

                    _.each(INDEXES, function(index) {
                        expect(this.selectedIndexesCollection.findWhere({name: index.name})).toBeDefined();
                    }, this);
                });

                it('should leave the indexes selected in the ui unchanged', function() {
                    expect(this.indexesView.$('i.hp-icon hp-check')).toHaveLength(0);
                });
            });

            describe('when selected indexes collection', function() {
                describe('is set to contain all but the first index', function() {
                    beforeEach(function() {
                        this.selectedIndexesCollection.set(_.tail(INDEXES));
                    });

                    it('should select the right indexes', function() {
                        var uncheckedCheckbox = this.idElement(INDEXES[0]).find('i');
                        var checkedCheckboxOne = this.idElement(INDEXES[1]).find('i');
                        var checkedCheckboxTwo = this.idElement(INDEXES[2]).find('i');

                        expect(uncheckedCheckbox).not.toHaveClass('hp-check');
                        expect(checkedCheckboxOne).toHaveClass('hp-check');
                        expect(checkedCheckboxTwo).toHaveClass('hp-check');
                    });
                });

                describe('is set to contain only first index', function() {
                    beforeEach(function() {
                        this.selectedIndexesCollection.set(_.head(INDEXES));
                    });

                    it('should select only the first index', function() {
                        var checkedCheckbox = this.idElement(INDEXES[0]).find('i');
                        var uncheckedCheckboxOne = this.idElement(INDEXES[1]).find('i');
                        var uncheckedCheckboxTwo = this.idElement(INDEXES[2]).find('i');

                        expect(checkedCheckbox).toHaveClass('hp-check');
                        expect(uncheckedCheckboxOne).not.toHaveClass('hp-check');
                        expect(uncheckedCheckboxTwo).not.toHaveClass('hp-check');
                    });
                });
            });
        });

    });

});