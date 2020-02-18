/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'handlebars',
    'find/app/page/search/template-helpers/typeof-helper'
], function (Handlebars, typeofHelper) {

    describe('Typeof helper', function() {

        beforeEach(function() {
            const handlebars = Handlebars.create();
            handlebars.registerHelper('typeof', typeofHelper);
            this.template = handlebars.compile('<div>{{typeof value}}</div>')
        });

        it('works for strings', function() {
            expect(this.template({ value: 'the string' })).toBe('<div>string</div>');
        });

        it('works for objects', function() {
            expect(this.template({ value: { some: 'object' } })).toBe('<div>object</div>');
        });

        it('works for numbers', function() {
            expect(this.template({ value: 1.23 })).toBe('<div>number</div>');
        });

    });

});
