define([
    'find/app/page/search/filters/parametric/parametric-select-modal-list-view',
    'backbone'
], function(SelectModalListView, Backbone) {

    describe('Select field modal list view', function() {
        beforeEach(function() {

            this.selectCollection = new Backbone.Collection();

            var field = new Backbone.Model({
                displayName: 'Teenage Mutant Ninja Turtles',
                id: 'TMNT',
                numeric: undefined
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

            this.parametricDisplayCollection = new Backbone.Collection([field]);

            this.view = new SelectModalListView({
                parametricDisplayCollection: this.parametricDisplayCollection,
                selectCollection: this.selectCollection,
                field: field
            });

            this.view.render();
        });

        it('should create 4 checkboxes', function() {
            expect(this.view.$('.i-check').length).toBe(4);
        });

        it('should display the counts correctly', function() {
            expect(this.view.$('label')[0]).toContainText('Leonardo');
            expect(this.view.$('label')[1]).toContainText('Michelangelo');
            expect(this.view.$('label')[2]).toContainText('Raphael');
            expect(this.view.$('label')[3]).toContainText('Donatello');
        });

        it('should have two selected and two unselected', function() {
            expect(this.view.$('input[checked]').length).toBe(2);
        });

        describe('After clicking on an unselected value', function() {
           beforeEach(function () {
               this.view.$('[data-id="Michelangelo"] ins').click();
           });
            
            it('should check the selected box', function () {
                expect(this.view.$('[data-id="Michelangelo"] [aria-checked="true"]')).not.toBeEmpty();
            });

            it('should add the selected item to the selectedCollection', function () {
                expect(this.selectCollection.length).toBe(1);
                var newField = this.selectCollection.models[0];

                expect(newField.get('displayName')).toBe('Teenage Mutant Ninja Turtles');
                expect(newField.get('field')).toBe('TMNT');
                expect(newField.get('numeric')).toBe(undefined);
                expect(newField.get('selected')).toBe(true);
                expect(newField.get('value')).toBe('Michelangelo');
            })
        });

        describe('After clicking on a previously selected value', function() {
            beforeEach(function () {
                this.view.$('[data-id="Raphael"] ins').click();
            });

            it('should uncheck the selected box', function () {
                expect(this.view.$('[data-id="Raphael"] [aria-checked="false"]')).not.toBeEmpty();
            });

            it('should add the selected item to the selectedCollection', function () {
                expect(this.selectCollection.length).toBe(1);
                var newField = this.selectCollection.models[0];

                expect(newField.get('displayName')).toBe('Teenage Mutant Ninja Turtles');
                expect(newField.get('field')).toBe('TMNT');
                expect(newField.get('numeric')).toBe(undefined);
                expect(newField.get('selected')).toBe(false);
                expect(newField.get('value')).toBe('Raphael');
            })
        });
    });

});
