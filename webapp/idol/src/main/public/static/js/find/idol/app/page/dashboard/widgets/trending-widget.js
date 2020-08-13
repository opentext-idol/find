/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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
    'jquery',
    'moment',
    'backbone',
    'i18n!find/nls/bundle',
    './saved-search-widget',
    'find/app/page/search/results/trending/trending-strategy',
    'find/app/page/search/results/trending/trending'
], function(_, $, moment, Backbone, i18n, SavedSearchWidget, trendingStrategy, Trending) {
    'use strict';

    const DEFAULT_NUMBER_OF_VALUES = 10;
    const DEFAULT_NUMBER_OF_BUCKETS = 20;

    return SavedSearchWidget.extend({
        viewType: 'trending',

        render: function() {
            SavedSearchWidget.prototype.render.call(this);

            this.$chart = $('<div class="full-height"></div>').appendTo(this.$content);

            this.trendingChart = new Trending({
                el: this.$chart.get(0),
                zoomEnabled: false,
                dragEnabled: false,
                hoverEnabled: false,
                yAxisLabelForUnit: i18n['search.resultsView.trending.yAxis'],
                yAxisUnitsText: function(yUnit) {
                    return i18n['search.resultsView.trending.unit.' + yUnit];
                }
            });
        },

        getData: function() {
            const fetchOptions = {
                queryModel: this.queryModel,
                model: this.model,
                field: this.widgetSettings.parametricField.id,
                dateField: this.widgetSettings.dateField.id,
                numberOfValuesToDisplay: this.widgetSettings.maxValues || DEFAULT_NUMBER_OF_VALUES,
                values: this.widgetSettings.values
            };

            return trendingStrategy.fetchField(fetchOptions)
                .then(function(selectedFieldValues) {
                    if(selectedFieldValues.length === 0) {
                        return $.when();
                    } else {
                        let rangePromise;

                        if(this.widgetSettings.minDate && this.widgetSettings.maxDate) {
                            rangePromise = $.when({
                                currentMax: this.widgetSettings.maxDate ? moment(this.widgetSettings.maxDate) : undefined,
                                currentMin: this.widgetSettings.minDate ? moment(this.widgetSettings.minDate) : undefined
                            });
                        } else {
                            rangePromise = trendingStrategy.fetchRange(selectedFieldValues, fetchOptions)
                                .then(function(range) {
                                    let currentMax = moment(this.widgetSettings.maxDate
                                        ? this.widgetSettings.maxDate
                                        : range.max);
                                    let currentMin = moment(this.widgetSettings.minDate
                                        ? this.widgetSettings.minDate
                                        : range.min);

                                    if(currentMin === currentMax) {
                                        currentMax.add(1, 'day');
                                        currentMin.subtract(1, 'day');
                                    }

                                    return {
                                        currentMax: currentMax,
                                        currentMin: currentMin
                                    }
                                }.bind(this));
                        }

                        return rangePromise.then(function(range) {
                            this.currentMin = range.currentMin;
                            this.currentMax = range.currentMax;

                            return trendingStrategy.fetchBucketedData(_.extend(fetchOptions, {
                                selectedFieldValues: selectedFieldValues,
                                targetNumberOfBuckets: this.widgetSettings.numberOfBuckets || DEFAULT_NUMBER_OF_BUCKETS,
                                currentMax: range.currentMax,
                                currentMin: range.currentMin
                            }));
                        }.bind(this));
                    }
                }.bind(this))
                .done(function() {
                    this.bucketedValues = Array.prototype.slice.call(arguments);
                }.bind(this));
        },

        isEmpty: function() {
            return _.isEmpty(this.bucketedValues);
        },

        updateVisualizer: function() {
            if(!this.isEmpty()) {
                const chartData = trendingStrategy.createChartData({
                    bucketedValues: this.bucketedValues,
                    currentMax: this.currentMax,
                    currentMin: this.currentMin
                });

                const data = chartData.data;
                const minDate = data[0].points[0].mid;
                const maxDate = data[data.length - 1].points[data[0].points.length - 1].mid;

                this.trendingChart.draw({
                    reloaded: true,
                    chartData: chartData,
                    minDate: minDate,
                    maxDate: maxDate
                });
            }
        },

        onResize: function() {
            this.updateVisualizer();
        },

        getSavedSearchRouterParameters: function() {
            const parametricField = this.widgetSettings && this.widgetSettings.parametricField;
            const fieldId = parametricField && parametricField.id;
            return fieldId ? '/' + encodeURIComponent(fieldId) : '';
        },

        exportData: function() {
            const colors = this.trendingChart.colors;

            if(this.isEmpty()) {
                return null;
            } else {
                const timestamps = this.bucketedValues[0].values.map(function(value) {
                    return value.min.clone().add(Math.floor(value.bucketSize / 2)).unix();
                });
                const rows = this.bucketedValues.map(function(bucketInfo, index) {
                    const color = bucketInfo.color
                        ? _.findWhere(colors, {name: bucketInfo.color})
                        : colors[index % colors.length];

                    return {
                        color: color.hex,
                        label: bucketInfo.valueName,
                        secondaryAxis: false,
                        values: _.pluck(bucketInfo.values, 'count')
                    }
                });

                return {
                    type: 'dategraph',
                    data: {
                        timestamps: timestamps,
                        rows: rows
                    }
                }
            }
        }
    });
});
