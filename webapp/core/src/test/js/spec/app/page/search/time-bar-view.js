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
            this.parametricFieldsCollection = new Backbone.Collection([
                {id: 'autn_date', displayName: 'Autn Date', type: 'NumericDate'}
            ]);

            this.timeBarModel = new Backbone.Model({
                graphedFieldId: 'autn_date',
                graphedFieldName: 'Autn Date',
                graphedDataType: 'NumericDate'
            });

            this.view = new TimeBarView({
                queryModel: this.queryModel,
                parametricFieldsCollection: this.parametricFieldsCollection,
                timeBarModel: this.timeBarModel,
                queryState: {
                    selectedParametricValues: this.selectedParametricValues
                }
            });

            this.view.render();
        });

        afterEach(function() {
            BucketedParametricCollection.Model.reset();
        });

        it('displays the prettified field name', function() {
            expect(this.view.$('h4')).toContainText('Autn Date');
        });

        it('clears the time bar model on clicking the cross', function() {
            this.view.$('.time-bar-container-icon').click();

            expect(this.timeBarModel.get('graphedFieldName')).toBeNull();
        });
    });

});
