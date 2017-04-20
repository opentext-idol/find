/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'i18n!find/nls/bundle',
    './saved-search-widget',
    'find/app/page/search/results/trending/trending-strategy',
    'find/app/page/search/results/trending/trending'
], function(_, $, Backbone, i18n, SavedSearchWidget, trendingStrategy, Trending) {
    'use strict';

    const colours = ['#1f77b4', '#6baed6', '#ff7f0e', '#e377c2', '#2ca02c', '#98df8a', '#d62728', '#ff9896', '#9467bd', '#e7ba52'];

    return SavedSearchWidget.extend({
        viewType: 'trending',

        render: function() {
            SavedSearchWidget.prototype.render.call(this);

            // TODO Implement consistent dashboard widget empty handling
            this.$emptyMessage = $('<div class="hide">' + i18n['search.resultsView.trending.empty'] + '</div>').appendTo(this.$content);

            this.$chart = $('<div class="full-height"></div>').appendTo(this.$content);

            this.trendingChart = new Trending({
                el: this.$chart.get(0),
                zoomEnabled: false,
                dragEnabled: false,
                hoverEnabled: false
            });
        },

        getData: function() {
            const fetchOptions = {
                queryModel: this.queryModel,
                selectedParametricValues: this.queryModel.queryState.selectedParametricValues,
                model: this.model,
                field: this.widgetSettings.parametricField.id,
                dateField: this.widgetSettings.dateField.id,
                numberOfValuesToDisplay: this.widgetSettings.maxValues,
            };

            return trendingStrategy.fetchField(fetchOptions)
                .then(function(selectedFieldValues) {
                    if(selectedFieldValues.length === 0) {
                        return $.when();
                    } else {
                        return trendingStrategy.fetchRange(selectedFieldValues, fetchOptions)
                            .then(function(model) {
                                this.currentMax = model.max;
                                this.currentMin = model.min;

                                return trendingStrategy.fetchBucketedData(_.extend(fetchOptions, {
                                    selectedFieldValues: selectedFieldValues,
                                    targetNumberOfBuckets: this.widgetSettings.numberOfBuckets,
                                    currentMax: this.currentMax,
                                    currentMin: this.currentMin
                                }));
                            }.bind(this));
                    }
                }.bind(this))
                .done(function() {
                    this.bucketedValues = Array.prototype.slice.call(arguments);
                }.bind(this));
        },

        updateVisualizer: function() {
            if(!_.isEmpty(this.bucketedValues)) {
                const data = trendingStrategy.createChartData({
                    bucketedValues: this.bucketedValues,
                    currentMax: this.currentMax,
                    currentMin: this.currentMin
                });

                const minDate = data[0].points[0].mid;
                const maxDate = data[data.length - 1].points[data[0].points.length - 1].mid;

                this.trendingChart.draw({
                    reloaded: true,
                    data: data,
                    minDate: minDate,
                    maxDate: maxDate,
                    yAxisLabel: i18n['search.resultsView.trending.yAxis']
                });
            }
        },

        exportData: function() {
            if (_.isEmpty(this.bucketedValues)) {
                return null;
            } else {
                const timestamps = this.bucketedValues[0].values.map(function(value) {
                    return (value.min + value.max) / 2;
                });
                const rows = this.bucketedValues.map(function(bucketInfo, index) {
                    return {
                        color: colours[index % colours.length],
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
