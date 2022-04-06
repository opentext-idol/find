/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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
    'find/app/page/search/filters/parametric/calibrate-buckets'
], function(calibrateBuckets) {
    'use strict';

    describe('calibrateBuckets', function() {
        it('returns one empty bucket when buckets is empty', function() {
            expect(calibrateBuckets([], [10.5, 12.9])).toEqual([
                {min: 10.5, max: 12.9, count: 0}
            ]);
        });

        it('returns one empty bucket when all buckets are less than the range', function() {
            expect(calibrateBuckets([
                {min: 0.5, max: 1.0, count: 100}
            ], [10.5, 12.9])).toEqual([
                {min: 10.5, max: 12.9, count: 0}
            ]);
        });

        it('returns one empty bucket when all buckets are greater than the range', function() {
            expect(calibrateBuckets([
                {min: 13.0, max: 14.0, count: 100}
            ], [10.5, 12.9])).toEqual([
                {min: 10.5, max: 12.9, count: 0}
            ]);
        });

        it('pads the start if the buckets do not span the lower section of the range', function() {
            expect(calibrateBuckets([
                {min: 11, max: 11.5, count: 100},
                {min: 11.5, max: 12, count: 50}
            ], [10, 12])).toEqual([
                {min: 10, max: 11, count: 0},
                {min: 11, max: 11.5, count: 100},
                {min: 11.5, max: 12, count: 50}
            ]);
        });

        it('pads the end if the buckets do not span the upper section of the range', function() {
            expect(calibrateBuckets([
                {min: 10, max: 10.5, count: 100},
                {min: 10.5, max: 11, count: 50}
            ], [10, 12])).toEqual([
                {min: 10, max: 10.5, count: 100},
                {min: 10.5, max: 11, count: 50},
                {min: 11, max: 12, count: 0}
            ]);
        });

        it('pads both ends if the buckets do not span the range', function() {
            expect(calibrateBuckets([
                {min: 2, max: 4, count: 50}
            ], [1, 5])).toEqual([
                {min: 1, max: 2, count: 0},
                {min: 2, max: 4, count: 50},
                {min: 4, max: 5, count: 0}
            ]);
        });

        it('includes buckets intersecting the range boundaries', function() {
            expect(calibrateBuckets([
                {min: 0, max: 2, count: 100},
                {min: 2, max: 4, count: 50},
                {min: 4, max: 6, count: 75}
            ], [1, 5])).toEqual([
                {min: 0, max: 2, count: 100},
                {min: 2, max: 4, count: 50},
                {min: 4, max: 6, count: 75}
            ]);
        });

        it('includes outer buckets touching the range exactly', function() {
            expect(calibrateBuckets([
                {min: 0, max: 2, count: 100},
                {min: 2, max: 4, count: 50},
                {min: 4, max: 6, count: 75},
                {min: 6, max: 8, count: 0}
            ], [2, 6])).toEqual([
                {min: 0, max: 2, count: 100},
                {min: 2, max: 4, count: 50},
                {min: 4, max: 6, count: 75},
                {min: 6, max: 8, count: 0}
            ]);
        });

        it('returns the original list if it spanned the range exactly', function() {
            expect(calibrateBuckets([
                {min: 0, max: 2, count: 100},
                {min: 2, max: 4, count: 50},
                {min: 4, max: 6, count: 75},
                {min: 6, max: 8, count: 0}
            ], [0, 8])).toEqual([
                {min: 0, max: 2, count: 100},
                {min: 2, max: 4, count: 50},
                {min: 4, max: 6, count: 75},
                {min: 6, max: 8, count: 0}
            ]);
        });
    });
});
