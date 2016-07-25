define([
    'find/app/page/search/filters/parametric/parametric-select-modal-view',
    'backbone'
], function(ParametricSelectModalView, Backbone) {

    describe('Parametric Select Modal View', function() {
        beforeEach(function() {
            this.selectCollection = new Backbone.Collection();

            var field = new Backbone.Model({
                displayName: 'Teenage Mutant Ninja Turtles',
                id: 'TMNT'
            });

            field.fieldValues = new Backbone.Collection([
                {
                    count: 100,
                    id: 'Leonardo',
                    selected: true
                },
                {
                    count: 75,
                    id: 'Michelangelo',
                    selected: false
                },
                {
                    count: 50,
                    id: 'Raphael',
                    selected: true
                },
                {
                    count: 1,
                    id: 'Donatello',
                    selected: false
                }
            ]);

            var parametricCollectionModel = new Backbone.Model({
                id: 'TMNT',
                values: [
                    {
                        count: 2000,
                        value: 'Leonardo'
                    },
                    {
                        count: 1000,
                        value: 'Michelangelo'
                    },
                    {
                        count: 500,
                        value: 'Raphael'
                    },
                    {
                        count: 200,
                        value: 'Donatello'
                    }
                ]
            });

            this.parametricDisplayCollection = new Backbone.Collection([field]);

            this.view = new ParametricSelectModalView({
                parametricCollection: new Backbone.Collection([parametricCollectionModel]),
                parametricDisplayCollection: this.parametricDisplayCollection,
                selectCollection: this.selectCollection,
                currentFieldGroup: 'TMNT'
            });

            this.view.render();
            this.view.$el.appendTo($('body'));
        });

        afterEach(function() {
            this.view.remove();
        });

        it('should display a loading spinner', function() {
            expect(this.view.$('.loading-spinner')).toBeVisible();
        });

        describe('after the renderFields callback is executed', function () {
            beforeEach(function () {
                this.view.renderFields();
            });

            it('should no longer display a loading spinner', function() {
                expect(this.view.$('.loading-spinner')).not.toExist();
            });

            it('should only show one field', function () {
                expect(this.view.$('.category-title')).toExist();
                expect(this.view.$('.category-title').length).toBe(1);
                expect(this.view.$('.category-title')).toHaveText('Teenage Mutant Ninja Turtles');
            });
        });
    });

});
