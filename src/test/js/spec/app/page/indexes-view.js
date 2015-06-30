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
            expect(this.indexesView.queryModel.set).not.toHaveBeenCalledWith('indexes', ['foo', 'bar']);

            this.indexesView.$('.list-indexes').click();

            this.indexesView.$('[name="wiki_eng"]').click();

            expect(this.indexesView.queryModel.set).toHaveBeenCalledWith('indexes', ['foo', 'bar']);
        });

        it("should contain an index after it has been reselected by clicking it twice", function() {
            this.indexesView.$('.list-indexes').click();

            this.indexesView.$('[name="wiki_eng"]').click().click();

            var finalCallCount = this.indexesView.queryModel.set.calls.count();
            var lastCallArguments = this.indexesView.queryModel.set.calls.mostRecent().args;

            expect(finalCallCount).toBe(4);
            expect(lastCallArguments[0]).toBe('indexes');
            expect(lastCallArguments[1]).toContain('wiki_eng');

        });

    });

});