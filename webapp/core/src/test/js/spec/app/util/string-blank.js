/*
 * Copyright 2016 Open Text.
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
    'find/app/util/string-blank'
], function(stringBlank) {

    describe('String blank', function() {
        it('returns true if the input is empty', function() {
            expect(stringBlank('')).toBe(true);
        });

        it('returns true if the input just contains whitespace', function() {
            expect(stringBlank(' \n')).toBe(true);
        });

        it('returns false if the input just contains word characters', function() {
            expect(stringBlank('foo')).toBe(false);
        });

        it('returns false if the input contains a mix of word characters and whitespace', function() {
            expect(stringBlank('  foo\n')).toBe(false);
        });
    });

});
