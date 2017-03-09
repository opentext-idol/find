/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/filters/parametric/numeric-parametric-field-collapsible-view',
    'backbone'
], function(NumericParametricFieldCollapsibleView, Backbone) {
    'use strict';

    describe('Numeric Parametric Field Collapsible View', function() {
        beforeEach(function() {
            this.filterModel = new Backbone.Model();

            this.view = new NumericParametricFieldCollapsibleView({
                selectedParametricValues: new Backbone.Collection(),
                type: 'Numeric',
                filterModel: this.filterModel,
                model: new Backbone.Model({id: 'the-model'})
            });

            this.view.render();
        });

        describe('when there is filter text', function() {
            beforeEach(function() {
                this.filterModel.set('text', 'the');
            });

            it('should open the view', function() {
                expect(this.view.collapseModel.get('collapsed')).toBe(false);
            });

            describe('and the filter text is removed again', function() {
                beforeEach(function() {
                    this.filterModel.set('text', '');
                });

                it('should hide the view', function() {
                    expect(this.view.collapseModel.get('collapsed')).toBe(true);
                });
            });
        });
    })
});
