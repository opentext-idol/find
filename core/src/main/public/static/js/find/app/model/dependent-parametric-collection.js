/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/find-base-collection'
], function(BaseCollection) {

    'use strict';
    
    function getArrayTotal(array) {
        return _.reduce(array, function(mem, val) { return mem + Number(val.count) }, 0);
    }
    
    function parseResult(array, total) {
        var minimumSize = Math.round(total / 100 * 5); // this is the smallest area of the chart an element will be visible at.
        var sunburstData = _.chain(array)
            .map(function(entry) {
                var entryHash = {
                    hidden: false,
                    text: entry.value,
                    count: Number(entry.count)
                };
                return _.isEmpty(entry.field) ? entryHash : _.extend(entryHash, {children: parseResult(entry.field, entry.count)}); // recurse for children
            })
            .filter(function(child) { //filter out any smaller than minimumSize
                return child.count > minimumSize;
            })
            .sortBy('count')
            .value();
        if (!_.isEmpty(sunburstData)) { //if there are items being displayed
            var childCount = getArrayTotal(sunburstData); // get total amount of displayed elements
            var remaining = total - childCount; // get the total amount of the hidden children
            if (remaining > minimumSize) {
                var dividersNo = Math.floor(remaining / minimumSize); // work out how many minimum size padding elements we need
                for (var i = 0; i < dividersNo; i++) {
                    sunburstData.unshift({text: '', hidden: true, count: minimumSize}); // add the padding containers
                }
                var remainingPadding = remaining - minimumSize * dividersNo; // get the remaining padding, it will rarely divide evenly into minimumSize pieces
                if (remainingPadding > 0) {
                    sunburstData.unshift({text: '', hidden: true, count: remainingPadding}); // add the remaining padding
                }
            }
            else if (remaining > 0){
                sunburstData.unshift({text: '', hidden: true, count: remaining}); // pad out the remaining area
            }
        }
        return sunburstData;
    }

    return BaseCollection.extend({
        url: '../api/public/parametric/second-parametric',

        parse: function(results) {
            var totalCount = getArrayTotal(results);

            return parseResult(results, totalCount);
        }
    });

});
