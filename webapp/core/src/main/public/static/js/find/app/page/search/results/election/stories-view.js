/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone',
    'jquery',
    'moment',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/results/election/stories-view.html',
    'find/app/model/theme-clusters-collection',
    'find/app/page/search/results/election/spectrograph'
], function(_, Backbone, $, moment, i18n,
            template, ThemeClustersCollection, Spectrograph) {
    'use strict';

    const DAY_COUNT = 7;
    const DAY_SECONDS = 24 * 60 * 60;
    const SPAN_SECONDS = DAY_COUNT * DAY_SECONDS;
    const END_DATE_UNIX = Math.floor(moment().unix() / DAY_SECONDS) * DAY_SECONDS + DAY_SECONDS;
    const START_DATE = moment.unix(END_DATE_UNIX - SPAN_SECONDS);
    const CHART_ASPECT_RATIO = 1.2;

    return Backbone.View.extend({
        template: _.template(template),

        initialize: function () {
            this.themeClustersCollection = new ThemeClustersCollection();
        },

        render: function () {
            this.$el.html(this.template({}));
            this.$container = this.$('.stories-graph');
            this.getData().then(_.bind(this.showGraph, this));
        },

        getData: function () {
            const requestData = {
                startDate: START_DATE.unix(),
                interval: SPAN_SECONDS
            };

            return this.themeClustersCollection.fetch({ data: requestData })
                .then(_.bind(function () {
                    return this.themeClustersCollection.models[0].get('clusters');
                }, this));
        },

        showGraph: function (clustersData) {
            Spectrograph({
                clustersData: clustersData,
                dayCount: DAY_COUNT,
                startDate: START_DATE,
                pageHeight: this.$container.width() / CHART_ASPECT_RATIO
            }, {
                parent: this.$container,
                spectrograph: this.$container.find('.spectrograph'),
                headings: this.$container.find('.headings')
            });
        }

    });

});
