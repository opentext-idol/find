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
