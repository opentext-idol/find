/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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
    'backbone',
    'find/app/page/search/filters/parametric/parametric-value-view'
], function(Backbone, ValueView) {
    'use strict';

    describe('Parametric value view', function() {
        beforeEach(function() {
            this.model = new Backbone.Model({
                value: 'cat',
                displayValue: 'feline',
                count: 3,
                selected: false
            });

            this.selectedValuesCollection = new Backbone.Collection();
            this.view = new ValueView({
                model: this.model,
                selectedValuesCollection: this.selectedValuesCollection
            });
            this.view.render();

            this.$check = this.view.$('.parametric-value-icon');
            this.$text = this.view.$('.parametric-value-text');
            this.$name = this.view.$('.parametric-value-name');
            this.$count = this.view.$('.parametric-value-count');
        });

        it('sets a data-value attribute', function() {
            expect(this.view.$el).toHaveAttr('data-value', 'cat');
        });

        it('displays the value name', function() {
            expect(this.$name).toContainText('feline');
        });

        it('displays the count', function() {
            expect(this.$count).toContainText('(3)');
        });

        it('hides the check icon if the value is not selected', function() {
            expect(this.$check).toHaveClass('hide');
        });

        describe('after the count is set to null', function() {
            beforeEach(function() {
                this.model.set('count', null);
                this.view.updateCount();
            });

            it('displays the value name', function() {
                expect(this.$name).toContainText('feline');
            });

            it('does not display the count', function() {
                expect(this.$count).not.toContainText('(3)');
                expect(this.$count).not.toContainText('(0)');
                expect(this.$count).not.toContainText('()');
                expect(this.$count).not.toContainText('null');
            });

            describe('then the count is set to a number', function() {
                beforeEach(function() {
                    this.model.set('count', 50);
                    this.view.updateCount();
                });

                it('displays the value name', function() {
                    expect(this.$name).toContainText('feline');
                });

                it('displays the count', function() {
                    expect(this.$count).toContainText('(50)');
                });
            });
        });

        describe('after the value is selected', function() {
            beforeEach(function() {
                this.selectedValuesCollection.add({id: 'cat'});
            });

            it('shows the check icon', function() {
                expect(this.$check).not.toHaveClass('hide');
            });

            describe('then the value is deselected', function() {
                beforeEach(function() {
                    this.selectedValuesCollection.remove('cat');
                });

                it('hides the check icon', function() {
                    expect(this.$check).toHaveClass('hide');
                });
            });
        });
    });
});
