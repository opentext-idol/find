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
    'underscore',
    'find/app/model/dependent-parametric-collection'
], function (_, DependentParametricCollection) {
    'use strict';

    // As this is mixed case, it can't match an IDOL field or a HOD field
    const NONE_COLUMN = 'defaultColumn';

    return DependentParametricCollection.extend({
        parse: function (data) {
            this.columnNames = _.chain(data)
                // take all the field arrays
                .pluck('subFields')
                // flatten into a single array so we can pluck the values
                .flatten()
                .pluck('displayValue')
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
                return _.map(data, function (datum) {
                    return {
                        count: datum.count,
                        text: datum.displayValue
                    }
                });
            } else {
                return _.map(data, function (datum) {
                    const columns = _.chain(datum.subFields)
                        .map(function (field) {
                            const value = {};
                            value[field.displayValue || NONE_COLUMN] = field.count;

                            return value;
                        })
                        .reduce(function (memo, fieldAndCount) {
                            return _.extend(memo, fieldAndCount);
                        }, {})
                        .value();

                    return _.extend({
                        text: datum.displayValue
                    }, columns);
                }, this);
            }
        }
    }, {
        noneColumn: NONE_COLUMN
    });
});
