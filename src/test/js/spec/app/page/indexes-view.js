define([
    'backbone',
    'find/app/page/indexes/indexes-view',
    'mock/model/indexes-collection'
], function(Backbone, IndexesView, IndexesCollection) {

    describe('Indexes View', function() {
        var INDEX = ['a','b','c'];

        var idElement = function(indexesView) {
            return function(id) {
                return indexesView.$("[data-id='" + id + "']");
            }
        };

        beforeEach(function() {
            IndexesCollection.reset();

            this.indexesCollection = new IndexesCollection();

            this.queryModel = new Backbone.Model();

            this.indexesView = new IndexesView({
                queryModel: this.queryModel,
                indexesCollection: this.indexesCollection
            });

            this.idElement = idElement(this.indexesView);

            this.indexesView.render();

            this.indexesCollection.set([
                {index: INDEX[0]},
                {index: INDEX[1]},
                {index: INDEX[2]}
            ]);

            this.indexesCollection.trigger('sync');
        });

        describe('after initialization', function() {

            it('should display indexes in the IndexesCollection', function() {
                var elements = this.indexesView.$el.find('[data-id]');
                var dataIds = _.map(elements, function (element) {
                    return $(element).attr('data-id')
                });

                expect(dataIds).toContain(INDEX[0]);
                expect(dataIds).toContain(INDEX[1]);
                expect(dataIds).toContain(INDEX[2]);
            });

            it('should select all the indexes by default in the ui', function() {
                var checkboxes = this.indexesView.$el.find('i');
                var checkboxHidden = _.find(checkboxes, function(checkbox) {
                    $(checkbox).hasClass('hide');
                });
                expect(checkboxHidden).not.toBeDefined();
            });

            it('should inform the query model with all the indexes by default', function() {
                var indexes = this.queryModel.get('indexes');

                expect(indexes).toContain(INDEX[0]);
                expect(indexes).toContain(INDEX[1]);
                expect(indexes).toContain(INDEX[2]);
            });

            describe('clicking an index once', function() {
                beforeEach(function() {
                    this.idElement(INDEX[0]).click();
                });

                it("should remove an index from the query model", function() {
                    expect(this.queryModel.get('indexes')).not.toContain(INDEX[0]);
                    expect(this.queryModel.get('indexes')).toContain(INDEX[1]);
                    expect(this.queryModel.get('indexes')).toContain(INDEX[2]);
                    expect(this.queryModel.get('allIndexesSelected')).toBe(false);
                });

                it("should uncheck the clicked index", function() {
                    var checkedCheckbox = this.idElement(INDEX[0]).parent().find('i');
                    var uncheckedCheckboxOne = this.idElement(INDEX[1]).parent().find('i');
                    var uncheckedCheckboxTwo = this.idElement(INDEX[2]).parent().find('i');

                    expect(checkedCheckbox.hasClass('hide')).toBe(true);
                    expect(uncheckedCheckboxOne.hasClass('hide')).toBe(false);
                    expect(uncheckedCheckboxTwo.hasClass('hide')).toBe(false);
                });
            });

            describe('clicking an index twice', function() {
                beforeEach(function() {
                    this.idElement(INDEX[0]).click().click();
                });

                it("should leave the query model unchanged", function() {
                    expect(this.queryModel.get('indexes')).toContain(INDEX[0]);
                    expect(this.queryModel.get('indexes')).toContain(INDEX[1]);
                    expect(this.queryModel.get('indexes')).toContain(INDEX[2]);
                    expect(this.queryModel.get('allIndexesSelected')).toBe(true);
                });

                it('should leave the indexes selected in the ui unchanged', function() {
                    var checkboxes = this.indexesView.$el.find('i');
                    var checkboxHidden = _.find(checkboxes, function(checkbox) {
                        $(checkbox).hasClass('hide');
                    });
                    expect(checkboxHidden).toBeFalsy();
                });
            });

            describe('removing all the indexes except one', function() {
                beforeEach(function() {
                    // click all the checkboxes except the first one
                    _.each(_.tail(INDEX), function(index) {
                        this.idElement(index).click();
                    }, this)
                });

                it('should disable the last item', function() {
                    expect(this.idElement(INDEX[0]).parent().hasClass('disabled-index')).toBe(true);
                });
            })

        });

        describe('when the query model', function() {
            describe('is set to contain all but the first index', function() {
                beforeEach(function() {
                    this.queryModel.set('indexes', _.tail(INDEX));
                });

                it('should select the right indexes', function() {
                    var checkedCheckbox = this.idElement(INDEX[0]).parent().find('i');
                    var uncheckedCheckboxOne = this.idElement(INDEX[1]).parent().find('i');
                    var uncheckedCheckboxTwo = this.idElement(INDEX[2]).parent().find('i');

                    expect(checkedCheckbox.hasClass('hide')).toBe(true);
                    expect(uncheckedCheckboxOne.hasClass('hide')).toBe(false);
                    expect(uncheckedCheckboxTwo.hasClass('hide')).toBe(false);
                });
            });

            describe('is set to contain only first index', function() {
                beforeEach(function() {
                    this.queryModel.set('indexes', INDEX[0]);
                });

                it('should select only the first index', function() {
                    var uncheckedCheckbox = this.idElement(INDEX[0]).parent().find('i');
                    var checkedCheckboxOne = this.idElement(INDEX[1]).parent().find('i');
                    var checkedCheckboxTwo = this.idElement(INDEX[2]).parent().find('i');

                    expect(uncheckedCheckbox.hasClass('hide')).toBe(false);
                    expect(checkedCheckboxOne.hasClass('hide')).toBe(true);
                    expect(checkedCheckboxTwo.hasClass('hide')).toBe(true);
                });

                it('should disable the first index', function() {
                    expect(this.idElement(INDEX[0]).parent().hasClass('disabled-index')).toBe(true);
                });
            })
        });

    });

});