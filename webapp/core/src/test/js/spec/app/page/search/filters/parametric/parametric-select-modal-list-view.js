define([
    'find/app/page/search/filters/parametric/parametric-select-modal-list-view',
    'backbone'
], function(SelectModalListView, Backbone) {

    describe('Parametric Select Modal list view', function() {
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
                field: field,
                allValues: [
                    { id: 'Leonardo' },
                    { id: 'Michelangelo' },
                    { id: 'Splinter' },
                    { id: 'Raphael' },
                    { id: 'Donatello' },
                    { id: 'Shredder' },
                    { id: 'Krang' }
                ]
            });

            this.view.render();
        });

        it('should create 7 checkboxes', function() {
            expect(this.view.$('.i-check').length).toBe(7);
        });

        it('should display the counts correctly', function() {
            var $labels = this.view.$('label');
            expect($labels[0]).toContainText('Leonardo (100)');
            expect($labels[1]).toContainText('Michelangelo (75)');
            expect($labels[2]).toContainText('Raphael (50)');
            expect($labels[3]).toContainText('Donatello (1)');
            expect($labels[4]).toContainText('Krang (0)');
            expect($labels[5]).toContainText('Shredder (0)');
            expect($labels[6]).toContainText('Splinter (0)');
        });

        it('should have two selected', function() {
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
