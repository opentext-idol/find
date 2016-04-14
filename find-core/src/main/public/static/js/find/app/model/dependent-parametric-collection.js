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

    return BaseCollection.extend({
        url: '../api/public/parametric/second-parametric',

        parse: function(results) {
            var totalCount = getArrayTotal(results);
            var parentFivePercent = Math.round(totalCount / 100 * 5);
            var data = _.chain(results)
                .map(function(result) {
                    var fivePercent = Math.round(result.count / 100 * 5);
                    var children = _.chain(result.field)
                        .map(function(child) {
                            return {
                                hidden: false,
                                text: child.value,
                                count: Number(child.count)
                            };
                        })
                        .filter(function(child) {
                            return child.count > fivePercent;
                        })
                        .sortBy('count')
                        .value();
                    if (!_.isEmpty(children)) {
                        var childCount = getArrayTotal(children);
                        var remaining = result.count - childCount;
                        var dividersNo = Math.round(remaining / fivePercent);
                        for (var i = 0; i < dividersNo; i++) {
                            children.unshift({text: 'padding', hidden: true, count: fivePercent})
                        }
                        children.unshift({text: 'padding', hidden: true, count: remaining - fivePercent  * dividersNo})
                    }
                    
                    return {
                        text: result.value,
                        count: Number(result.count),
                        children: children,
                        hidden: false
                    };
                })
                .sortBy('count')
                .filter(function(parent) {
                    return parent.count > parentFivePercent;
                })
                .value();

            if (!_.isEmpty(data)) {
                var newCount = getArrayTotal(data);
                var parentRemaining = totalCount - newCount;
                var dividersNo = Math.round(parentRemaining / parentFivePercent);
                for (var i = 0; i < dividersNo; i++) {
                    data.unshift({text: 'padding', hidden: true, count: parentFivePercent, children: []})
                }
                data.unshift({text: 'padding', hidden: true, count: parentRemaining - parentFivePercent * dividersNo});
            }

            return data;
        }
    });

});
