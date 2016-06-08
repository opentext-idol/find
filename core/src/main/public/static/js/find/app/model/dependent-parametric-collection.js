/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/find-base-collection'
], function (BaseCollection) {

    'use strict';

    function getArrayTotal(array) {
        return _.reduce(array, function (mem, val) {
            return mem + Number(val.count)
        }, 0);
    }

    function parseResult(array, total) {
        var minimumSize = Math.round(total / 100 * 5); // this is the smallest area of the chart an element will be visible at.

        var sunburstData = _.chain(array)
            .map(function (entry) {
                var entryHash = {
                    hidden: false,
                    text: entry.value,
                    count: Number(entry.count)
                };
                return _.isEmpty(entry.field) ? entryHash : _.extend(entryHash, {children: parseResult(entry.field, entry.count)}); // recurse for children
            })
            .sortBy('count')
            .reverse()
            .value();

        // Always show the highest 20 results
        var alwaysShownValues = _.first(sunburstData, 20);

        //filter out any with document counts smaller than minimumSize
        var filteredSunburstData = _.chain(sunburstData)
            .filter(function(child) {
                return child.count > minimumSize;
            })
            .sortBy('count')
            .value();

        sunburstData = _.union(alwaysShownValues, filteredSunburstData);

        if (!_.isEmpty(sunburstData)) { //if there are items being displayed
            var childCount = getArrayTotal(sunburstData); // get total displayed document count
            var remaining = total - childCount; // get the total hidden document count
            var hiddenFilterCount = array.length - sunburstData.length;  // get the number of hidden values
            if(remaining > 0){
                sunburstData.push({
                    text: '',
                    hidden: true,
                    count: remaining,
                    hiddenFilterCount: hiddenFilterCount
                });
            }
        }
        return sunburstData;
    }

    return BaseCollection.extend({
        url: '../api/public/parametric/dependent-values',

        parse: function (results) {
            var totalCount = getArrayTotal(results);

            return parseResult(results, totalCount);
        }
    });

});
