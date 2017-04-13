/*
 *  Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 *  Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'd3',
    'backbone',
    'i18n!find/nls/bundle',
    'find/app/configuration',
    'find/app/vent',
    'find/app/util/generate-error-support-message',
    'find/app/page/search/results/parametric-results-view',
    'find/app/page/search/results/field-selection-view',
    'find/app/page/search/filters/parametric/calibrate-buckets',
    'find/app/model/bucketed-parametric-collection',
    'find/app/model/parametric-field-details-model',
    'find/app/model/parametric-collection',
    'find/app/page/search/results/trending/trending',
    'parametric-refinement/to-field-text-node',

], function (_, $, d3, Backbone, i18n, configuration, vent, generateErrorHtml, ParametricResultsView, FieldSelectionView,
             calibrateBuckets, BucketedParametricCollection, ParametricDetailsModel, ParametricCollection, Trending,
             toFieldTextNode) {
    'use strict';

    const MILLISECONDS_TO_SECONDS = 1000;

    function fetchField(options) {
        const trendingCollection = new ParametricCollection([], {url: 'api/public/parametric/values'});
        return trendingCollection.fetch({
            data: {
                fieldNames: [options.field],
                databases: options.queryModel.get('indexes'),
                queryText: options.queryModel.get('autoCorrect') && options.queryModel.get('correctedQuery')
                    ? options.queryModel.get('correctedQuery')
                    : options.queryModel.get('queryText'),
                fieldText: toFieldTextNode(getFieldText(options.selectedParametricValues)),
                minDate: options.queryModel.getIsoDate('minDate'),
                maxDate: options.queryModel.getIsoDate('maxDate'),
                minScore: options.queryModel.get('minScore'),
                maxValues: options.values ? null : options.numberOfValuesToDisplay,
                valueRestrictions: options.values ? _.pluck(options.values, 'name') : null
            }
        }).then(function (fieldList) {
            const field = fieldList.filter(function (fieldData) {
                return fieldData.id === options.field;
            }, this);

            return field[0] ? field[0].values : [];
        });
    }

    function fetchRange(selectedFieldValues, options) {
        const trendingValues = _.first(selectedFieldValues, options.numberOfValuesToDisplay);
        const trendingValuesRestriction = 'MATCH{' + _.pluck(trendingValues, 'value').toString() + '}:' + options.field;
        const fieldText = getFieldText(options.selectedParametricValues).length > 0 ?
            ' AND ' + toFieldTextNode(getFieldText(options.selectedParametricValues))
            : '';

        return new ParametricDetailsModel().fetch({
            data: {
                fieldName: options.dateField,
                queryText: options.queryModel.get('queryText'),
                fieldText: trendingValuesRestriction + fieldText,
                minDate: options.queryModel.getIsoDate('minDate'),
                maxDate: options.queryModel.getIsoDate('maxDate'),
                minScore: options.queryModel.get('minScore'),
                databases: options.queryModel.get('indexes')
            }
        });
    }

    function fetchBucketedData(options) {
        const valuesToShow = _.first(options.selectedFieldValues, options.numberOfValuesToDisplay);

        return $.when.apply($, _.map(valuesToShow, function (value) {
            const bucketModel = new BucketedParametricCollection.Model({
                id: options.dateField,
                valueName: value.value
            });

            const fieldText = getFieldText(options.selectedParametricValues).length > 0 ?
                ' AND ' + toFieldTextNode(getFieldText(options.selectedParametricValues))
                : '';

            return bucketModel
                .fetch({
                    data: {
                        queryText: options.queryModel.get('queryText'),
                        fieldText: 'MATCH{' + value.value + '}:' + options.field + fieldText,
                        minDate: options.queryModel.getIsoDate('minDate'),
                        maxDate: options.queryModel.getIsoDate('maxDate'),
                        minScore: options.queryModel.get('minScore'),
                        databases: options.queryModel.get('indexes'),
                        targetNumberOfBuckets: options.targetNumberOfBuckets,
                        bucketMin: options.currentMin,
                        bucketMax: options.currentMax
                    }
                })
                .then(function (data) {
                    return _.extend({
                        valueName: value.value,
                        color: options.values ? _.findWhere(options.values, {'name': value.value}).color : null
                    }, data);
                });
        }));
    }

    function getFieldText(selectedParametricValues) {
        return selectedParametricValues.map(function (model) {
            return model.toJSON();
        });
    }


    function createChartData(options) {
        const data = [];

        _.each(options.bucketedValues, function (bucketedValue) {
            data.push({
                points: _.map(bucketedValue.values, function (value) {
                    return {
                        count: value.count,
                        mid: Math.floor(value.min + ((value.max - value.min) / 2)),
                        min: value.min,
                        max: value.max
                    };
                }),
                name: bucketedValue.valueName,
                color: bucketedValue.color
            });
        });

        _.each(data, function (value) {
            _.each(value.points, function (point) {
                point.mid = new Date(point.mid * MILLISECONDS_TO_SECONDS);
                point.min = new Date(point.min * MILLISECONDS_TO_SECONDS);
                point.max = new Date(point.max * MILLISECONDS_TO_SECONDS);
            });
        });

        return adjustBuckets(data, options.currentMin, options.currentMax);
    }

    function adjustBuckets(values, min, max) {
        return _.map(values, function (value) {
            return {
                name: value.name,
                color: value.color,
                points: _.filter(value.points, function (point) {
                    const date = new Date(point.mid).getTime() / MILLISECONDS_TO_SECONDS;
                    return date >= min && date <= max;
                })
            }
        });
    }

    return {
        fetchField: fetchField,
        fetchRange: fetchRange,
        fetchBucketedData: fetchBucketedData,
        createChartData: createChartData
    }
});