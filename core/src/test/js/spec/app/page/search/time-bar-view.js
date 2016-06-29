define([
    'backbone',
    'jquery',
    'find/app/page/search/time-bar-view',
    'find/app/model/bucketed-parametric-collection'
], function(Backbone, $, TimeBarView, BucketedParametricCollection) {

    describe('TimeBarView', function() {
        beforeEach(function() {
            this.queryModel = new Backbone.Model();
            this.selectedParametricValues = new Backbone.Collection();
            this.previewModeModel = new Backbone.Model({document: null});
            
            this.view = new TimeBarView({
                queryModel: this.queryModel,
                previewModeModel: this.previewModeModel,
                queryState: {
                    selectedParametricValues: this.selectedParametricValues
                }
            });

            this.view.$el.appendTo($(document.body));
            this.view.render();
        });

        afterEach(function() {
            BucketedParametricCollection.Model.reset();
            this.view.remove();
        });

        it('fetches the AUTN_DATE field', function() {
            var bucketModel = BucketedParametricCollection.Model.instances[0];
            expect(bucketModel.get('id')).toBe('autn_date');
            expect(bucketModel.sync).toHaveBeenCalledWith('read', jasmine.any(Object), jasmine.any(Object));
        });

        it('displays the loading indicator', function() {
            expect(this.view.$('.loading-spinner')).toBeVisible();
        });

        describe('once the fetch succeeds', function() {
            beforeEach(function() {
                BucketedParametricCollection.Model.instances[0].sync.calls.argsFor(0)[2].success({
                    bucketSize: 100,
                    count: 12,
                    min: 0,
                    max: 400,
                    name: 'autn_date',
                    id: 'autn_date',
                    values: [
                        {count: 1, min: 0, max: 100},
                        {count: 0, min: 100, max: 200},
                        {count: 7, min: 200, max: 300},
                        {count: 4, min: 300, max: 400}
                    ]
                });

                BucketedParametricCollection.Model.syncPromises[0].resolve();
            });

            it('removes the loading spinner', function() {
                expect(this.view.$('.loading-spinner')).toHaveLength(0);
            });
        });
    });

});
