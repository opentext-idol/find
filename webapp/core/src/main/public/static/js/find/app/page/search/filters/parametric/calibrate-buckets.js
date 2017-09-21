/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore'
], function(_) {
    'use strict';

    function emptyBucket(range) {
        return {min: range[0], max: range[1], count: 0};
    }

    /**
     * @typedef {Object} Buckets
     * @property {Number} min
     * @property {Number} max
     * @property {Number} count
     */
    /**
     * Fill the specified range with buckets. Uses the given buckets if they lie
     * within the range, the rest of the range is padded with empty buckets. Note
     * that the bucket size is not respected.
     * @param {Buckets[]} buckets Must be ordered and have no gaps
     * @param {Number[]} range Must have length 2 ([min, max])
     * @return {Buckets[]} Ordered, complete buckets filling the given range exactly
     */
    function calibrateBuckets(buckets, range) {
        const filteredBuckets = _.filter(buckets, function(value) {
            return !(value.min < range[0] || value.max > range[1]);
        });

        if(filteredBuckets.length === 0) {
            // None of the given buckets with in the range
            return [emptyBucket(range)];
        } else {
            const filteredMin = _.first(filteredBuckets).min;
            const preBuckets = filteredMin > range[0]
                ? [emptyBucket([range[0], filteredMin])]
                : [];

            const filteredMax = _.last(filteredBuckets).max;
            const postBuckets = filteredMax < range[1]
                ? [emptyBucket([filteredMax, range[1]])]
                : [];

            return preBuckets.concat(filteredBuckets).concat(postBuckets);
        }
    }

    return calibrateBuckets;
});
