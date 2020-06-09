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
    'find/app/model/find-base-collection'
], function(_, BaseCollection) {
    'use strict';

    function getArrayTotal(array) {
        return _.reduce(array, function (mem, val) {
            return mem + val.count;
        }, 0);
    }

    function parseResult (array, total, minShownResults) {
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
            .sortBy('text')
            .sortBy(function (x) {
                return -x.count;
            })
            .value();

        // Always show the highest results
        const alwaysShownValues = _.first(initialSunburstData, minShownResults || 20);

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
        idAttribute: 'text',
        url: 'api/public/parametric/dependent-values',

        initialize: function(opts) {
            BaseCollection.prototype.initialize.apply(this, arguments);
            const options = opts || {};
            this.minShownResults = options.minShownResults;
        },

        parse: function(results) {
            return parseResult(results, getArrayTotal(results), this.minShownResults);
        },

        fetchDependentFields: function(queryModel, primaryField, secondaryField) {
            return this.fetch({
                data: {
                    databases: queryModel.get('indexes'),
                    queryText: queryModel.get('queryText'),
                    fieldText: queryModel.get('fieldText')
                        ? queryModel.get('fieldText').toString()
                        : '',
                    minDate: queryModel.getIsoDate('minDate'),
                    maxDate: queryModel.getIsoDate('maxDate'),
                    minScore: queryModel.get('minScore'),
                    fieldNames: secondaryField
                        ? [primaryField, secondaryField]
                        : [primaryField],
                    stateTokens: queryModel.get('stateMatchIds')
                }
            });
        }
    });
});
