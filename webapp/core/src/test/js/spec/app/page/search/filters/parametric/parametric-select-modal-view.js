define([
    'find/app/page/search/filters/parametric/parametric-select-modal-view',
    'backbone'
], function(ParametricSelectModalView, Backbone) {

    describe('Parametric Select Modal View', function() {
        beforeEach(function() {
            this.selectedParametricValues = new Backbone.Collection([
                {field: 'AUTHOR', displayName: 'Author', value: 'Matthew', displayValue: 'Matthew', type: 'Parametric'}
            ]);

            const queryModel = new Backbone.Model({
                indexes: ['WIKIPEDIA'],
                autoCorrect: false,
                queryText: 'cat',
                fieldText: null,
                minScore: 50,
                stateTokens: []
            });

            queryModel.getIsoDate = jasmine.createSpy('getIsoDate').and.returnValue(null);

            this.view = new ParametricSelectModalView({
                queryModel: queryModel,
                initialField: 'CATEGORY',
                selectedParametricValues: this.selectedParametricValues,
                indexesCollection: new Backbone.Collection([
                    {name: 'BROADCAST'},
                    {name: 'WIKIPEDIA'}
                ]),
                parametricFieldsCollection: new Backbone.Collection([
                    {id: 'AUTHOR', displayName: 'Author', type: 'Parametric'},
                    {id: 'PLACE', displayName: 'Place', type: 'Parametric'},
                    {id: 'CATEGORY', displayName: 'Category', type: 'Parametric'}
                ])
            });

            this.view.render();
        });

        afterEach(function() {
            this.view.remove();
        });

        it('renders each field as a tab', function() {
            const $tabs = this.view.$('.fields-list a');
            expect($tabs).toHaveLength(3);
            expect($tabs.eq(0)).toContainText('Author');
            expect($tabs.eq(1)).toContainText('Place');
            expect($tabs.eq(2)).toContainText('Category');
        });

        it('selects the initialField', function() {
            expect(this.view.$('.fields-list [data-field="CATEGORY"]')).toHaveClass('active');

            expect(this.view.$('.fields-list [data-field="AUTHOR"]')).not.toHaveClass('active');
            expect(this.view.$('.fields-list [data-field="PLACE"]')).not.toHaveClass('active');
        });

        it('creates a list view for each field', function() {
            expect(this.view.$('.values-list .tab-pane')).toHaveLength(3);
        });

        describe('when a different field tab is clicked', function() {
            beforeEach(function() {
                this.view.$('.fields-list [data-field="AUTHOR"] a').click();
            });

            it('selects the new field', function() {
                expect(this.view.$('.fields-list [data-field="AUTHOR"]')).toHaveClass('active');

                expect(this.view.$('.fields-list [data-field="CATEGORY"]')).not.toHaveClass('active');
                expect(this.view.$('.fields-list [data-field="PLACE"]')).not.toHaveClass('active');
            });
        });
    });

});
