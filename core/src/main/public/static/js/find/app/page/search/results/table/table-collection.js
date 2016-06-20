/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/find-base-collection',
    'underscore'
], function(BaseCollection, _) {
    'use strict';

    // As this is mixed case, it can't match an IDOL field or a HOD field
    var NONE_COLUMN = 'defaultColumn';
    
    return BaseCollection.extend({
        url: '../api/public/parametric/dependent-values',

        parse: function(data) {
            this.columnNames = _.chain(data)
            // take all the field arrays
                .pluck('field')
                // flatten into a single array so we can pluck the values
                .flatten()
                .pluck('value')
                // make unique and sort
                .uniq()
                .sort()
                .value();

            if (_.contains(this.columnNames, '')) {
                // remove '' and replace it with our magic name at the front if it exists as a value
                this.columnNames = _.without(this.columnNames, '');

                this.columnNames.unshift(NONE_COLUMN);
            }

            if (_.isEmpty(this.columnNames)) {
                return _.map(data, function(datum) {
                    return {
                        count: Number(datum.count),
                        text: datum.value
                    }
                });
            }
            else {
                return _.map(data, function(datum) {
                    var columns = _.chain(datum.field)
                        .map(function(field) {
                            var value = {};
                            value[field.value || NONE_COLUMN] = Number(field.count);

                            return value;
                        })
                        .reduce(function(memo, fieldAndCount) {
                            return _.extend(memo, fieldAndCount);
                        }, {})
                        .value();

                    return _.extend({
                        text: datum.value
                    }, columns);
                }, this);
            }
        }
    }, {
        noneColumn: NONE_COLUMN
    });
    
});