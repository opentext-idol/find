/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the &quot;License&quot;); you may not use this file except in compliance with the License.
 */

define([
    'handlebars',
    'find/app/page/search/template-helpers/capitalise-helper'
], function (Handlebars, capitaliseHelper) {

    describe('Capitalise helper', function() {

        beforeEach(function() {
            const handlebars = Handlebars.create();
            handlebars.registerHelper('capitalise', capitaliseHelper);
            this.template = handlebars.compile('<div>{{capitalise value}}</div>')
        });

        it('single word', function() {
            expect(this.template({ value: 'word' })).toBe('<div>Word</div>');
        });

        it('multiple words', function() {
            expect(this.template({ value: 'one two Three' })).toBe('<div>One two Three</div>');
        });

        it('already capitalised', function() {
            expect(this.template({ value: 'WORD' })).toBe('<div>WORD</div>');
        });

        it('empty string', function() {
            expect(this.template({ value: '' })).toBe('<div></div>');
        });

    });

});
