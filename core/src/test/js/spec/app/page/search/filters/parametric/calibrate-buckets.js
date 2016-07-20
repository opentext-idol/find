/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/filters/parametric/calibrate-buckets'
], function(calibrateBuckets) {

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
                {min: 12.9, max: 13.0, count: 100}
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

        it('pads both ends if the range boundaries intersect the bucket boundaries', function() {
            expect(calibrateBuckets([
                {min: 0, max: 2, count: 100},
                {min: 2, max: 4, count: 50},
                {min: 4, max: 6, count: 75}
            ], [1, 5])).toEqual([
                {min: 1, max: 2, count: 0},
                {min: 2, max: 4, count: 50},
                {min: 4, max: 5, count: 0}
            ]);
        });

        it('does not pad the start or end if a subset of the buckets spans the range exactly', function() {
            expect(calibrateBuckets([
                {min: 0, max: 2, count: 100},
                {min: 2, max: 4, count: 50},
                {min: 4, max: 6, count: 75},
                {min: 6, max: 8, count: 0}
            ], [2, 6])).toEqual([
                {min: 2, max: 4, count: 50},
                {min: 4, max: 6, count: 75}
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
