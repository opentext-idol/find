/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/filters/parametric/parametric-field-view',
    'backbone',
    'parametric-refinement/selected-values-collection'
], function(FieldView, Backbone, SelectedParametricValues) {
    'use strict';

    describe('Parametric field view', function() {
        beforeEach(function() {
            this.queryModel = new Backbone.Model({queryText: '*'});

            this.model = new Backbone.Model({
                displayName: 'Primary Author',
                id: 'primary_author'
            });

            this.parametricCollection = new Backbone.Collection([
                {id: 'primary_author', displayName: 'Primary Author', totalValues: 1500, values: [
                    {value: 'bob', displayValue: 'bob', count: 100},
                    {value: 'penny', displayValue: 'penny', count: 96},
                    {value: 'fred', displayValue: 'fred', count: 25}
                ]}
            ]);

            this.selectedParametricValues = new SelectedParametricValues([
                {field: 'primary_author', displayName: 'Primary Author', value: 'bob', displayValue: 'bob', type: 'Parametric'},
                {field: 'primary_author', displayName: 'Primary Author', value: 'penny', displayValue: 'penny', type: 'Parametric'},
            ]);
        });

        describe('with a collapse boolean', function() {
            beforeEach(function() {
                this.fieldView = new FieldView({
                    queryModel: this.queryModel,
                    model: this.model,
                    parametricCollection: this.parametricCollection,
                    selectedParametricValues: this.selectedParametricValues,
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
                expect(this.fieldView.collapsible.collapseModel.get('collapsed')).toBe(false);
            });
        });

        describe('with a collapse function', function() {
            beforeEach(function() {
                this.fieldView = new FieldView({
                    queryModel: this.queryModel,
                    model: this.model,
                    parametricCollection: this.parametricCollection,
                    selectedParametricValues: this.selectedParametricValues,
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
                expect(this.fieldView.collapsible.collapseModel.get('collapsed')).toBe(true);
            });
        });

        describe('with a header', function() {
            beforeEach(function() {
                this.fieldView = new FieldView({
                    queryModel: this.queryModel,
                    model: this.model,
                    parametricCollection: this.parametricCollection,
                    selectedParametricValues: this.selectedParametricValues,
                    collapsed: false
                });
                this.fieldView.render();
            });

            it('that counts selected fields', function() {
                expect(this.fieldView.$el).toContainText('(2 / 1500)');
            });

            it('that displays (X) and not (0 / X) when no fields are selected', function() {
                this.selectedParametricValues.reset();
                expect(this.fieldView.$el).toContainText('(1500)');
            });
        });
    });
});
