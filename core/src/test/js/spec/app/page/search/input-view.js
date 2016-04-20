/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/input-view',
    'backbone'
], function(InputView, Backbone) {

    describe('Input view', function() {
        beforeEach(function() {
            this.model = new Backbone.Model({
                inputText: 'cat',
                relatedConcepts: [['lion'], ['tiger']]
            });

            this.view = new InputView({
                model: this.model
            });

            this.view.render();
        });

        it('displays the initial search text', function() {
            expect(this.view.$('.find-input').typeahead('val')).toBe('cat');
        });

        it('displays the initial related concepts', function() {
            expect(this.view.$('.additional-concepts')).toContainText('lion');
            expect(this.view.$('.additional-concepts')).toContainText('tiger');
        });

        it('updates the text when the model changes', function() {
            this.model.set('inputText', 'dog');

            expect(this.view.$('.find-input').typeahead('val')).toBe('dog');
        });

        it('updates the related concepts when the model changes', function() {
            this.model.set('relatedConcepts', []);

            expect(this.view.$('.additional-concepts')).not.toContainText('lion');
            expect(this.view.$('.additional-concepts')).not.toContainText('tiger');
        });

        it('updates the model text and clears the related concepts when the input is changed', function() {
            this.view.$('.find-input').typeahead('val', 'dog');
            this.view.$('.find-form').submit();

            expect(this.model.get('inputText')).toBe('dog');
            expect(this.model.get('relatedConcepts')).toEqual([]);
        });

        it('updates the model concepts when a related concept is removed', function() {
            this.view.$('[data-id="0"] .concept-remove-icon').click();

            expect(this.model.get('relatedConcepts')).toEqual([['tiger']]);
        });
    });

});
