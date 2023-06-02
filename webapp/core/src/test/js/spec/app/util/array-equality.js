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
    'find/app/util/array-equality',
    'underscore'
], function(arrayEquality, _) {
    'use strict';

    describe('Array Equality', function() {
        it('returns true for two null values', function() {
            expect(arrayEquality(null, null)).toBe(true);
        });

        it('returns false for a null value and an array', function() {
            expect(arrayEquality(null, [])).toBe(false);
        });

        it('returns false for an array and a null value ', function() {
            expect(arrayEquality([], null)).toBe(false);
        });

        it('returns true for two empty arrays', function() {
            expect(arrayEquality([], [])).toBe(true);
        });

        it('returns true for identical arrays', function() {
            expect(arrayEquality([1, 2], [1, 2])).toBe(true);
        });

        it('returns true for arrays with identical elements in different orders', function() {
            expect(arrayEquality([1, 2], [2, 1])).toBe(true);
        });

        it('returns false for arrays with different lengths', function() {
            expect(arrayEquality([1, 1], [1])).toBe(false);
            expect(arrayEquality([1], [1, 1])).toBe(false);
        });

        it('returns false for arrays with different elements', function() {
            expect(arrayEquality([1, 1], [1, 2])).toBe(false);
            expect(arrayEquality([1, 2], [1, 1])).toBe(false);
        });

        it('returns false for arrays with different numbers of the same elements', function() {
            expect(arrayEquality([1, 1, 2, 2], [1, 2, 2, 2])).toBe(false);
        });

        it('does not mutate its arguments', function() {
            var input = [1, 2];
            arrayEquality([1, 2], input);
            expect(_.isEqual(input, [1, 2])).toBe(true);
        });

        it('returns true for identical arrays of blobs', function() {
            expect(arrayEquality(
                [
                    {a: 'a'}
                ], [
                    {a: 'a'}
                ],
                _.isEqual
            )).toBe(true);
        });

        it('returns true for arrays with identical blobs', function() {
            expect(arrayEquality(
                [
                    {one: 'one', 1: 1},
                    {two: 'two', 2: 2},
                    {three: 'three', 3: 3}
                ], [
                    {three: 'three', 3: 3},
                    {one: 'one', 1: 1},
                    {two: 'two', 2: 2}
                ],
                _.isEqual
            )).toBe(true);
        });
    });
});
