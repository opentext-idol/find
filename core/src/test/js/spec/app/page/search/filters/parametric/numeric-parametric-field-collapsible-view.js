/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/filters/parametric/numeric-parametric-field-collapsible-view',
    'backbone'
], function(NumericParametricFieldCollapsibleView) {
    'use strict';

    describe('Numeric Parametric Field Collapsible View', function() {
        beforeEach(function() {
            this.filterModel = new Backbone.Model();

            this.view = new NumericParametricFieldCollapsibleView({
                selectedParametricValues: new Backbone.Collection(),
                dataType: 'Numeric',
                filterModel: this.filterModel,
                model: new Backbone.Model({id: 'the-model'})
            });

            this.view.render();
        });

        it('should open the view when there is filter text', function() {
            this.filterModel.set('text', 'the');

            expect(this.view.collapsible.collapsed).toBe(false);
        });

        it('should remember the view state after the view is closed', function() {
            spyOn(this.view.collapsible, 'toggle');

            expect(this.view.collapsible.collapsed).toBe(true);

            this.filterModel.set('text', 'the');

            expect(this.view.collapsible.collapsed).toBe(false);

            this.filterModel.set('text', '');

            expect(this.view.collapsible.toggle.calls.count()).toBe(1);
            expect(this.view.collapsible.toggle.calls.argsFor(0)[0]).toBe(false);
        });
    })

});