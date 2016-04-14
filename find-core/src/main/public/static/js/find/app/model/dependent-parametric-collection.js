/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/find-base-collection'
], function(BaseCollection) {

    'use strict';

    return BaseCollection.extend({
        url: '../api/public/parametric/second-parametric',

        parse: function(results) {
            var totalCount = _.reduce(results, function(mem, val) { return mem + Number(val.count) }, 0);
            return _.chain(results)
                .map(function(result) {
                    var children = _.chain(result.field)
                        .map(function(child) {
                            return {
                                hidden: child.count / result.count * 100 < 5,
                                text: child.value,
                                count: Number(child.count)
                            };
                        })
                        .sortBy('count')
                        .value();

                    return {
                        text: result.value,
                        count: Number(result.count),
                        children: children,
                        hidden: result.count / totalCount * 100 < 5
                    };
                })
                .sortBy('count')
                .value();
        }
    });

});
