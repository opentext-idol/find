define([
    'backbone',
    'find/app/page/indexes/indexes-view',
    'mock/model/indexes-collection'
], function(Backbone, IndexesView, IndexesCollection) {

    describe('Indexes View', function() {
        var I = ['a','b','c'];

        beforeEach(function() {
            IndexesCollection.reset();

            this.queryModel = new Backbone.Model();

            this.indexesView = new IndexesView({
                queryModel: this.queryModel
            });

            this.indexesView.idElement = function(id) {
                return this.$("[data-id='" + id + "']")
            };

            this.indexesView.render();

            $('body').append(this.indexesView.el);

            IndexesCollection.instances[0].set([
                {index: I[0]},
                {index: I[1]},
                {index: I[2]}
            ]);

            IndexesCollection.instances[0].trigger('sync');
        });

        afterEach(function() {
            this.indexesView.remove();
        });

        describe('after initialization', function() {

            it('should display indexes in the IndexesCollection', function() {
                var elements = this.indexesView.$el.find('[data-id]');
                var dataIDs = _.map(elements, function (element) {
                    return $(element).attr('data-id')
                });

                expect(dataIDs).toContain(I[0]);
                expect(dataIDs).toContain(I[1]);
                expect(dataIDs).toContain(I[2]);
            });

            it('should select all the indexes by default in the ui', function() {
                var checkboxes = this.indexesView.$el.find('i');
                var checkboxHidden = _.find(checkboxes, function(checkbox) {
                    $(checkbox).hasClass('hide');
                });
                expect(checkboxHidden).toBeFalsy();
            });

            it('should inform the query model with all the indexes by default', function() {
                var indexes = this.queryModel.get('indexes');

                expect(indexes).toContain(I[0]);
                expect(indexes).toContain(I[1]);
                expect(indexes).toContain(I[2]);
            });

            describe('clicking an index once', function() {
                beforeEach(function() {
                    this.indexesView.idElement(I[0]).click();
                });

                it("should remove an index from the query model", function() {
                    expect(this.queryModel.get('indexes')).not.toContain(I[0]);
                    expect(this.queryModel.get('indexes')).toContain(I[1]);
                    expect(this.queryModel.get('indexes')).toContain(I[2]);
                    expect(this.queryModel.get('allIndexesSelected')).toBe(false);
                });

                it("should uncheck the clicked index", function() {
                    var checkboxA = this.indexesView.idElement(I[0]).parent().find('i');
                    expect(checkboxA.hasClass('hide')).toBe(true);

                    var checkboxB = this.indexesView.idElement(I[1]).parent().find('i');
                    var checkboxC = this.indexesView.idElement(I[2]).parent().find('i');
                    expect(checkboxB.hasClass('hide')).toBe(false);
                    expect(checkboxC.hasClass('hide')).toBe(false);
                });
            });

            describe('clicking an index twice', function() {
                beforeEach(function() {
                    this.indexesView.idElement(I[0]).click().click();
                });

                it("should leave the query model unchanged", function() {
                    expect(this.queryModel.get('indexes')).toContain(I[0]);
                    expect(this.queryModel.get('indexes')).toContain(I[1]);
                    expect(this.queryModel.get('indexes')).toContain(I[2]);
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
                    _.each(I.slice(1), function(index) {
                        this.indexesView.idElement(index).click();
                    }, this)
                });

                it('should disable the last item', function() {
                    expect(this.indexesView.idElement(I[0]).parent().hasClass('disabled-index')).toBe(true);
                });
            })

        });

        describe('when the query model', function() {
            describe('is set to contain all but the first index', function() {
                beforeEach(function() {
                    this.queryModel.set('indexes', I.slice(1));
                });

                it('should select the right indexes', function() {
                    var checkboxA = this.indexesView.idElement(I[0]).parent().find('i');
                    expect(checkboxA.hasClass('hide')).toBe(true);

                    var checkboxB = this.indexesView.idElement(I[1]).parent().find('i');
                    var checkboxC = this.indexesView.idElement(I[2]).parent().find('i');
                    expect(checkboxB.hasClass('hide')).toBe(false);
                    expect(checkboxC.hasClass('hide')).toBe(false);
                });
            });

            describe('is set to contain only first index', function() {
                beforeEach(function() {
                    this.queryModel.set('indexes', I[0]);
                });

                it('should select only the first index', function() {
                    var checkboxA = this.indexesView.idElement(I[0]).parent().find('i');
                    expect(checkboxA.hasClass('hide')).toBe(false);

                    var checkboxB = this.indexesView.idElement(I[1]).parent().find('i');
                    var checkboxC = this.indexesView.idElement(I[2]).parent().find('i');
                    expect(checkboxB.hasClass('hide')).toBe(true);
                    expect(checkboxC.hasClass('hide')).toBe(true);
                });

                it('should disable the first index', function() {
                    expect(this.indexesView.idElement(I[0]).parent().hasClass('disabled-index')).toBe(true);
                });
            })
        });

    });

});