/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'find/app/page/search/filters/parametric/parametric-field-view',
    'backbone',
    'underscore',
    'parametric-refinement/selected-values-collection'
], function (FieldView, Backbone, _, SelectedParametricValues) {
    'use strict';

    describe('Parametric field view', function () {
        beforeEach(function () {
            this.queryModel = new Backbone.Model({queryText: '*'});

            this.model = new Backbone.Model({
                displayName: 'Primary Author',
                id: 'primary_author'
            });

            this.filteredParametricCollection = new Backbone.Collection([
                {
                    id: 'primary_author', displayName: 'Primary Author', totalValues: 1500, values: [
                    {value: 'bob', displayValue: 'bob', count: 100},
                    {value: 'penny', displayValue: 'penny', count: 96},
                    {value: 'fred', displayValue: 'fred', count: 25}
                ]
                }
            ]);
            this.filteredParametricCollection.isProcessing = _.noop;

            this.selectedParametricValues = new SelectedParametricValues([
                {
                    field: 'primary_author',
                    displayName: 'Primary Author',
                    value: 'bob',
                    displayValue: 'bob',
                    type: 'Parametric'
                },
                {
                    field: 'primary_author',
                    displayName: 'Primary Author',
                    value: 'penny',
                    displayValue: 'penny',
                    type: 'Parametric'
                },
            ]);
        });

        describe('with a collapse boolean', function () {
            beforeEach(function () {
                this.fieldView = new FieldView({
                    queryModel: this.queryModel,
                    model: this.model,
                    filteredParametricCollection: this.filteredParametricCollection,
                    selectedParametricValues: this.selectedParametricValues,
                    collapsed: false
                });
                this.fieldView.render();
            });

            it('sets a data-field attribute', function () {
                expect(this.fieldView.$el).toHaveAttr('data-field', 'primary_author');
            });

            it('displays the display name', function () {
                expect(this.fieldView.$el).toContainText('Primary Author');
            });

            it('displays the field values', function () {
                expect(this.fieldView.$('[data-value="bob"]')).toHaveLength(1);
                expect(this.fieldView.$('[data-value="penny"]')).toHaveLength(1);
                expect(this.fieldView.$('[data-value="fred"]')).toHaveLength(1);
            });

            it('should set the collapsed property', function () {
                expect(this.fieldView.collapsible.collapseModel.get('collapsed')).toBe(false);
            });

            it('hides the error message', function () {
                expect(this.fieldView.$('.parametric-value-error')).toHaveClass('hide');
            });

            describe('then the parametric values collection is fetched', function () {
                beforeEach(function () {
                    this.filteredParametricCollection.trigger('request');
                });

                it('displays the title loading spinner', function () {
                    expect(this.fieldView.$('.parametric-field-title-processing-indicator')).not.toHaveClass('hide');
                });

                it('displays the collapsible loading text', function () {
                    expect(this.fieldView.$('.parametric-value-processing-indicator')).not.toHaveClass('hide');
                });

                it('hides the counts', function () {
                    expect(this.fieldView.$('.parametric-value-counts')).toHaveClass('hide');
                });

                describe('then the request fails', function () {
                    beforeEach(function () {
                        this.filteredParametricCollection.trigger('error', this.filteredParametricCollection, {status: 500});
                    });

                    it('hides the title loading spinner', function () {
                        expect(this.fieldView.$('.parametric-field-title-processing-indicator')).toHaveClass('hide');
                    });

                    it('hides the collapsible loading text', function () {
                        expect(this.fieldView.$('.parametric-value-processing-indicator')).toHaveClass('hide');
                    });

                    it('displays the error message', function () {
                        expect(this.fieldView.$('.parametric-value-error')).not.toHaveClass('hide');
                    });

                    it('hides the counts', function () {
                        expect(this.fieldView.$('.parametric-value-counts')).toHaveClass('hide');
                    });
                });

                describe('then the request is aborted', function () {
                    beforeEach(function () {
                        this.filteredParametricCollection.trigger('error', this.filteredParametricCollection, {status: 0});
                    });

                    it('hides the error message', function () {
                        expect(this.fieldView.$('.parametric-value-error')).toHaveClass('hide');
                    });
                });

                describe('then the request succeeds', function () {
                    beforeEach(function () {
                        this.filteredParametricCollection.trigger('sync');
                    });

                    it('hides the title loading spinner', function () {
                        expect(this.fieldView.$('.parametric-field-title-processing-indicator')).toHaveClass('hide');
                    });

                    it('hides the collapsible loading text', function () {
                        expect(this.fieldView.$('.parametric-value-processing-indicator')).toHaveClass('hide');
                    });

                    it('hides the error message', function () {
                        expect(this.fieldView.$('.parametric-value-error')).toHaveClass('hide');
                    });

                    it('displays the counts', function () {
                        expect(this.fieldView.$('.parametric-value-counts')).not.toHaveClass('hide');
                    });
                });
            });
        });

        describe('with a collapse function', function () {
            beforeEach(function () {
                this.filterModel = new Backbone.Model();
                this.fieldView = new FieldView({
                    queryModel: this.queryModel,
                    model: this.model,
                    filteredParametricCollection: this.filteredParametricCollection,
                    selectedParametricValues: this.selectedParametricValues,
                    filterModel: this.filterModel,
                    collapsed: function (model) {
                        return model.id === 'primary_author'
                    }
                });
                this.fieldView.render();
            });

            it('sets a data-field attribute', function () {
                expect(this.fieldView.$el).toHaveAttr('data-field', 'primary_author');
            });

            it('displays the display name', function () {
                expect(this.fieldView.$el).toContainText('Primary Author');
            });

            it('displays the field values', function () {
                expect(this.fieldView.$('[data-value="bob"]')).toHaveLength(1);
                expect(this.fieldView.$('[data-value="penny"]')).toHaveLength(1);
                expect(this.fieldView.$('[data-value="fred"]')).toHaveLength(1);
            });

            it('should set the collapsed property', function () {
                expect(this.fieldView.collapsible.collapseModel.get('collapsed')).toBe(true);
            });

            describe('when there is filter text', function () {
                beforeEach(function () {
                    this.filterModel.set('text', 'the');
                });

                it('should open the view', function () {
                    expect(this.fieldView.collapseModel.get('collapsed')).toBe(false);
                });

                describe('and the filter text is removed again', function () {
                    beforeEach(function () {
                        this.filterModel.set('text', '');
                    });

                    it('should hide the view', function () {
                        expect(this.fieldView.collapseModel.get('collapsed')).toBe(true);
                    });
                });
            });
        });

        describe('with a header', function () {
            beforeEach(function () {
                this.fieldView = new FieldView({
                    queryModel: this.queryModel,
                    model: this.model,
                    filteredParametricCollection: this.filteredParametricCollection,
                    selectedParametricValues: this.selectedParametricValues,
                    collapsed: false
                });
                this.fieldView.render();
                this.filteredParametricCollection.trigger('sync')
            });

            it('that counts selected fields', function () {
                expect(this.fieldView.$el).toContainText('(2 / 1500)');
            });

            it('that displays (X) and not (0 / X) when no fields are selected', function () {
                this.selectedParametricValues.reset();
                expect(this.fieldView.$el).toContainText('(1500)');
            });
        });
    });
});
