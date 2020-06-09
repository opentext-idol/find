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
