/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/find-base-collection',
    'underscore'
], function (BaseCollection, _) {
    'use strict';

    function getArrayTotal(array) {
        return _.reduce(array, function (mem, val) {
            return mem + val.count
        }, 0);
    }

    function parseResult(array, total) {
        const minimumSize = Math.round(total / 100 * 5); // this is the smallest area of the chart an element will be visible at.

        const initialSunburstData = _.chain(array)
            .filter(function (element) {
                return element.value !== '';
            })
            .map(function (entry) {
                const entryHash = {
                    hidden: false,
                    text: entry.displayValue,
                    underlyingValue: entry.value,
                    count: entry.count
                };
                return _.isEmpty(entry.subFields) ? entryHash : _.extend(entryHash, {children: parseResult(entry.subFields, entry.count)}); // recurse for children
            })
            .sortBy('id')
            .sortBy(function (x) {
                return -x.count
            })
            .value();

        // Always show the highest 20 results
        const alwaysShownValues = _.first(initialSunburstData, 20);

        //filter out any with document counts smaller than minimumSize
        const filteredSunburstData = _.chain(initialSunburstData)
            .filter(function (child) {
                return child.count > minimumSize;
            })
            .value();

        const sunburstData = _.union(alwaysShownValues, filteredSunburstData);

        if (!_.isEmpty(sunburstData)) { //if there are items being displayed
            const childCount = getArrayTotal(sunburstData); // get total displayed document count
            const remaining = total - childCount; // get the total hidden document count
            const hiddenFilterCount = initialSunburstData.length - sunburstData.length;  // get the number of hidden values
            if (remaining > 0) {
                sunburstData.push({
                    text: '',
                    underlyingValue: '',
                    hidden: true,
                    count: remaining,
                    hiddenFilterCount: hiddenFilterCount
                });
            }
        }
        return sunburstData;
    }

    return BaseCollection.extend({
        url: 'api/public/parametric/dependent-values',

        parse: function (results) {
            const totalCount = getArrayTotal(results);

            return parseResult(results, totalCount);
        }
    });
});
