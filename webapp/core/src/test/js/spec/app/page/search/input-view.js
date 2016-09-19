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
                inputText: 'cat'
            });

            this.view = new InputView({
                model: this.model
            });

            this.view.render();
        });

        it('displays the initial search text', function() {
            expect(this.view.$('.find-input').typeahead('val')).toBe('cat');
        });

        it('updates the text when the model changes', function() {
            this.model.set('inputText', 'dog');

            expect(this.view.$('.find-input').typeahead('val')).toBe('dog');
        });

        it('updates the model text when the input is changed', function() {
            this.view.$('.find-input').typeahead('val', 'dog');
            this.view.$('.find-form').submit();

            expect(this.model.get('inputText')).toBe('dog');
        });
    });

});
