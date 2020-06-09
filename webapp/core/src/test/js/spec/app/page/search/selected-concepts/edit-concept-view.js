/*
 * (c) Copyright 2015-2016 Micro Focus or one of its affiliates.
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
    'find/app/page/search/selected-concepts/edit-concept-view',
    'backbone',
    'jquery'
], function(EditConceptView, Backbone, $) {

    describe('EditConceptView', function() {
        beforeEach(function() {
            this.conceptModel = new Backbone.Model({
                concepts: ['cat', 'bob']
            });

            this.view = new EditConceptView({
                model: this.conceptModel
            });

            this.view.render();
            $('body').append(this.view.$el);
        });

        it('displays a textarea with all the concepts in the model new line separated', function() {
            expect(this.view.$('textarea').val()).toBe('cat\nbob');
        });

        it('saves the new concepts if user inputs new concepts and clicks save', function() {
            this.view.$('textarea').val('cats\nwinston');
            this.view.$('.edit-concept-confirm-button').click();

            expect(this.conceptModel.get('concepts')).toEqual(['cats', 'winston']);
        });

        it('saves concepts that are separated by new line and quoted', function() {
            this.view.$('textarea').val('cats\n"winston churchill"\n\n"bob\nmarley"\n"foo');
            this.view.$('.edit-concept-confirm-button').click();

            expect(this.conceptModel.get('concepts')).toEqual(['cats', '"winston churchill"', '"bob marley"', 'foo']);
        });

        it('saves concepts that are separated by new line and quoted and have other text on same line', function() {
            this.view.$('textarea').val('cats "winston churchill"\n\n"bob\n\n\nmarley" test\n"foo');
            this.view.$('.edit-concept-confirm-button').click();

            expect(this.conceptModel.get('concepts')).toEqual(['cats "winston churchill"', '"bob marley" test', 'foo']);
        });

        it('saves concepts with braces', function() {
            this.view.$('textarea').val('(bonds OR "tax")\n("tax relief" AND taxes)');
            this.view.$('.edit-concept-confirm-button').click();

            expect(this.conceptModel.get('concepts')).toEqual(['(bonds OR "tax")', '("tax relief" AND taxes)']);
        });
    });
});
