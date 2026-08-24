/*
 * Copyright 2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

const StaticContentWidget = require('./widgets/static-content-widget');
const StaticImageWidget = require('./widgets/static-image-widget');
const MapWidget = require('./widgets/map-widget');
const SunburstWidget = require('./widgets/sunburst-widget');
const TopicMapWidget = require('./widgets/topic-map-widget');
const TimeLastRefreshedWidget = require('./widgets/time-last-refreshed-widget');
const CurrentTimeWidget = require('./widgets/current-time-widget');
const ResultsListWidget = require('./widgets/results-list-widget');
const VideoWidget = require('./widgets/video-widget');
const TrendingWidget = require('./widgets/trending-widget');

const registry = {
    StaticContentWidget: {
        Constructor: StaticContentWidget
    },
    StaticImageWidget: {
        Constructor: StaticImageWidget
    },
    MapWidget: {
        Constructor: MapWidget
    },
    SunburstWidget: {
        Constructor: SunburstWidget
    },
    TopicMapWidget: {
        Constructor: TopicMapWidget
    },
    TimeLastRefreshedWidget: {
        Constructor: TimeLastRefreshedWidget
    },
    CurrentTimeWidget: {
        Constructor: CurrentTimeWidget
    },
    ResultsListWidget: {
        Constructor: ResultsListWidget
    },
    VideoWidget: {
        Constructor: VideoWidget
    },
    TrendingWidget: {
        Constructor: TrendingWidget
    }
};

module.exports = function(widget) {
        return registry[widget];
    };

