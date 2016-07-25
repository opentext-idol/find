define([
    'find/app/page/search/filters/parametric/parametric-field-view',
    'backbone'
], function(FieldView, Backbone) {
    'use strict';

    describe('Parametric field view', function() {
        beforeEach(function() {
            this.model = new Backbone.Model({
                displayName: 'Primary Author',
                id: 'primary_author'
            });

            this.model.fieldValues = new Backbone.Collection([
                {id: 'bob', count: 100, selected: true},
                {id: 'penny', count: 96, selected: true},
                {id: 'fred', count: 25, selected: false}
            ]);
        });

        describe('with a collapse boolean', function() {
            beforeEach(function() {
                this.fieldView = new FieldView({
                    model: this.model,
                    selectedParametricValues: new Backbone.Collection(),
                    collapsed: false
                });
                this.fieldView.render();
            });

            it('sets a data-field attribute', function() {
                expect(this.fieldView.$el).toHaveAttr('data-field', 'primary_author');
            });

            it('displays the display name', function() {
                expect(this.fieldView.$el).toContainText('Primary Author');
            });

            it('displays the field values', function() {
                expect(this.fieldView.$('[data-value="bob"]')).toHaveLength(1);
                expect(this.fieldView.$('[data-value="penny"]')).toHaveLength(1);
                expect(this.fieldView.$('[data-value="fred"]')).toHaveLength(1);
            });

            it('should set the collapsed property', function() {
                expect(this.fieldView.collapsible.collapsed).toBe(false);
            });
        });

        describe('with a collapse function', function() {
            beforeEach(function() {
                this.fieldView = new FieldView({
                    model: this.model,
                    selectedParametricValues: new Backbone.Collection(),
                    collapsed: function(model) {
                        return model.id === 'primary_author'
                    }
                });
                this.fieldView.render();
            });

            it('sets a data-field attribute', function() {
                expect(this.fieldView.$el).toHaveAttr('data-field', 'primary_author');
            });

            it('displays the display name', function() {
                expect(this.fieldView.$el).toContainText('Primary Author');
            });

            it('displays the field values', function() {
                expect(this.fieldView.$('[data-value="bob"]')).toHaveLength(1);
                expect(this.fieldView.$('[data-value="penny"]')).toHaveLength(1);
                expect(this.fieldView.$('[data-value="fred"]')).toHaveLength(1);
            });

            it('should set the collapsed property', function() {
                expect(this.fieldView.collapsible.collapsed).toBe(true);
            });
        })

    });

});