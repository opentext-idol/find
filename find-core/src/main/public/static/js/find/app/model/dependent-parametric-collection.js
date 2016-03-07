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
            return _.chain(results)
                .map(function(result) {
                    var children = _.chain(result.field)
                        .map(function(child) {
                            return {
                                text: child.value,
                                count: Number(child.count)
                            };
                        })
                        .sortBy('count')
                        .last(10)
                        .value();

                    return {
                        text: result.value,
                        count: Number(result.count),
                        children: children
                    };
                })
                .sortBy('count')
                .last(10)
                .value();
        }
    });

});
