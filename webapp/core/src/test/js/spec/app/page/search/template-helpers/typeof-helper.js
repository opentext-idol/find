/*
 * Copyright 2020 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
