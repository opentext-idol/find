/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
