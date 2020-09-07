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
    'find/app/page/search/results/field-selection-view',
    'text!find/templates/app/page/search/results/election/polls-view.html',
    'find/app/model/polling-data-collection'
], function(_, Backbone, moment, i18n, Trending, TrendingStrategy, FieldSelectionView,
            template, PollingDataCollection) {
    'use strict';

    const END_DATE = moment();
    const PARTIES = {
        DEM: { name: 'Democrat', colour: '#1668c1' },
        REP: { name: 'Republican', colour: '#ff454f' }
    };
    const TIME_SCALES = [
        { id: '6m', displayName: 'Last 6 Months',
            startDate: END_DATE.clone().subtract(6, 'month'),
            bucketCount: 30 },
        { id: '6w', displayName: 'Last 6 Weeks',
            startDate: END_DATE.clone().subtract(6, 'week') ,
            bucketCount: 20 },
        { id: '2w', displayName: 'Last 2 Weeks',
            startDate: END_DATE.clone().subtract(2, 'week'),
            bucketCount: 10 }
    ];
    const CHART_ASPECT_RATIO = 1.8;

    return Backbone.View.extend({
        template: _.template(template),

        initialize: function () {
            this.timescaleModel = new Backbone.Model({ field: '6m' });

            this.listenTo(this.timescaleModel, 'change:field', this.showGraph);
        },

        render: function () {
            this.$el.html(this.template({}));

            this.timescaleSelector = new FieldSelectionView({
                model: this.timescaleModel,
                name: 'timescale',
                fields: TIME_SCALES,
                allowEmpty: false
            });
            this.$('.polls-timescale-selector').prepend(this.timescaleSelector.$el);
            this.timescaleSelector.render();

            this.$container = this.$('.polls-graph');
            this.$container.height(Math.ceil(this.$container.width() / CHART_ASPECT_RATIO));

            this.getData().then(_.bind(function (data) {
                this.data = this.processData(data);
                this.showGraph();
            }, this));
        },

        getData: function () {
            const promises = _.map(TIME_SCALES, _.bind(function (timeScale) {
                const requestData = {
                    bucketCount: timeScale.bucketCount,
                    bucketMin: timeScale.startDate.toISOString(),
                    bucketMax: END_DATE.toISOString()
                };

                const collection = new PollingDataCollection();
                return collection.fetch({ data: requestData }).then(function () {
                    return collection.map(function (model) {
                        return model.toJSON();
                    });
                });
            }, this));

            return $.when.apply($, promises).then(function () {
                return _.object(_.pluck(TIME_SCALES, 'id'), _.toArray(arguments));
            });
        },

        processData: function (data) {
            return _.mapObject(data, function (timeScaleData, timeScaleId) {
                const processedData = _.chain(timeScaleData)
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
                    timeScale: _.findWhere(TIME_SCALES, { id: timeScaleId }),
                    chartData: {
                        data: processedData,
                        minRate: _.max([0, min - padding]),
                        maxRate: max + padding,
                        yUnit: '%'
                    }
                };
            });
        },

        showGraph: function () {
            if (this.data !== undefined) {
                this.$container.html('');
                const chart = new Trending({
                    el: this.$container.get(0),
                    tooltipText: i18n['search.resultsView.polls.tooltipText'],
                    zoomEnabled: false,
                    dragEnabled: false,
                    hoverEnabled: true,
                    yAxisLabelForUnit: _.constant('Poll Average (%)'),
                    yAxisUnitsText: _.constant('%')
                });

                const data = this.data[this.timescaleModel.get('field')];
                chart.draw({
                    reloaded: false,
                    chartData: data.chartData,
                    minDate: data.timeScale.startDate.toDate(),
                    maxDate: END_DATE.toDate()
                });
            }
        }

    });

});
