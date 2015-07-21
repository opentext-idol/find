define([
    'mock/backbone-mock-factory',
    'find/app/page/indexes/indexes-view',
    'mock/model/indexes-collection'
], function(mockFactory, IndexesView, IndexesCollection) {

    describe('Indexes View', function() {
        beforeEach(function() {
            IndexesCollection.reset();

            var Model = mockFactory.getModel(['get', 'set']);

            this.indexesView = new IndexesView({
                queryModel: new Model()
            });

            this.indexesView.render();

            $('body').append(this.indexesView.el);

            IndexesCollection.instances[0].set([
                {index: 'wiki_eng'},
                {index: 'foo'},
                {index: 'bar'}
            ]);

            IndexesCollection.instances[0].trigger('sync');
        });

        afterEach(function() {
            this.indexesView.remove();
        });

        it("should not contain an index after it has been deselected by clicking it once", function() {
            this.indexesView.$('.list-indexes').click();

            this.indexesView.$('[data-id="wiki_eng"]').click();

            var callArgument = this.indexesView.queryModel.set.calls.mostRecent().args[0];
            expect(callArgument.indexes).toContain('foo');
            expect(callArgument.indexes).toContain('bar');
            expect(callArgument.allIndexesSelected).toBe(false);
        });

        it("should contain an index after it has been reselected by clicking it twice", function() {
            this.indexesView.$('[data-id="wiki_eng"]').click().click();

            var finalCallCount = this.indexesView.queryModel.set.calls.count();
            var callArgument = this.indexesView.queryModel.set.calls.mostRecent().args[0];

            expect(finalCallCount).toBe(4);
            expect(callArgument.indexes).toContain('foo');
            expect(callArgument.indexes).toContain('bar');
            expect(callArgument.indexes).toContain('wiki_eng');
            expect(callArgument.allIndexesSelected).toBe(true);

        });

    });

});