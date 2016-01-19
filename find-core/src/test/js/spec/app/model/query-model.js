define([
    'find/app/model/backbone-query-model',
    'find/app/model/query-model'
], function(BackboneQueryModel, QueryModel) {

    describe('Query Model', function() {
        beforeEach(function() {
            var backboneQueryModel = new BackboneQueryModel();

            this.queryModel = new QueryModel(backboneQueryModel);

            this.changed = false;

            this.queryModel.on('change', function() {
                this.changed = true;
            }, this);
        });

        it("should change with query text set", function() {
            this.queryModel.set('queryText', 'text');

            expect(this.changed).toBe(true);
        });

        it("should not change with just indexes set", function() {
            this.queryModel.set('indexes', ['index1','index2']);

            expect(this.changed).toBe(false);
        });

        it("should change with both queryText and indexes set", function() {
            this.queryModel.set('indexes', ['index1','index2']);

            expect(this.changed).toBe(false);

            this.queryModel.set('queryText', 'text');

            expect(this.changed).toBe(true);
        });

        it("should proxy set events down to the underlying model", function() {
            this.queryModel.set('queryText', 'text');

            expect(this.queryModel.model.get('queryText')).toEqual('text');
        });

        it("should proxy get events down to the underlying model", function() {
            this.queryModel.model.set('queryText', 'text');

            expect(this.queryModel.get('queryText')).toEqual('text');
        });
    });
});