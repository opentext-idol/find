/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone',
    'moment',
    'i18n!find/nls/bundle',
    'find/app/page/search/results/trending/trending',
    'find/app/page/search/results/trending/trending-strategy',
    'text!find/templates/app/page/search/results/election/polls-view.html',
    'find/app/model/polling-data-collection'
], function(_, Backbone, moment, i18n, Trending, TrendingStrategy,
            template, PollingDataCollection) {
    'use strict';

    const BUCKET_COUNT = 30;
    const END_DATE = moment();
    const START_DATE = END_DATE.clone().subtract(6, 'month');
    const PARTIES = {
        DEM: { name: 'Democrat', colour: '#1668c1' },
        REP: { name: 'Republican', colour: '#ff454f' }
    };
    const CHART_ASPECT_RATIO = 1.8;

    return Backbone.View.extend({
        template: _.template(template),

        initialize: function () {
            this.pollingDataCollection = new PollingDataCollection();
        },

        render: function () {
            this.$el.html(this.template({}));
            this.$container = this.$('.polls-graph');
            this.$container.height(Math.ceil(this.$container.width() / CHART_ASPECT_RATIO));
            this.getData().then(_.bind(function (data) {
                this.showGraph(this.processData(data));
            }, this));
        },

        getData: function () {
            const requestData = {
                bucketCount: BUCKET_COUNT,
                bucketMin: START_DATE.toISOString(),
                bucketMax: END_DATE.toISOString()
            };

            return this.pollingDataCollection.fetch({ data: requestData });
        },

        processData: function (data) {
            const processedData = _.chain(data)
                .filter(function (partyData) {
                    return PARTIES[partyData.id];
                })
                .map(function (partyData) {
                    return {
                        name: PARTIES[partyData.id].name,
                        color: PARTIES[partyData.id].colour,
                        points: _.map(partyData.values, function (value) {
                            const midTime = moment(value.min)
                                .add(Math.floor(value.bucketSize / 2), 'seconds');
                            return {
                                rate: value.count / 10,
                                mid: midTime.toDate(),
                                min: moment(value.min).toDate(),
                                max: moment(value.max).toDate()
                            };
                        })
                    };
                })
                .value();

            const min = _.chain(processedData)
                .pluck('points').flatten().pluck('rate').min().value();
            const max = _.chain(processedData)
                .pluck('points').flatten().pluck('rate').max().value();
            const padding = 0.7 * (max - min);
            return {
                data: processedData,
                minRate: _.max([0, min - padding]),
                maxRate: max + padding,
                yUnit: '%'
            };
        },

        showGraph: function (chartData) {
            const chart = new Trending({
                el: this.$container.get(0),
                tooltipText: i18n['search.resultsView.polls.tooltipText'],
                zoomEnabled: false,
                dragEnabled: false,
                hoverEnabled: true,
                yAxisLabelForUnit: _.constant('Poll Average (%)'),
                yAxisUnitsText: _.constant('%')
            });

            chart.draw({
                reloaded: false,
                chartData: chartData,
                minDate: START_DATE.toDate(),
                maxDate: END_DATE.toDate()
            });
        }

    });

});
