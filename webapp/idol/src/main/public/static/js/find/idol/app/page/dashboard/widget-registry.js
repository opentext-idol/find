/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    './widgets/static-content-widget',
    './widgets/static-image-widget',
    './widgets/map-widget',
    './widgets/sunburst-widget',
    './widgets/topic-map-widget',
    './widgets/time-last-refreshed-widget',
    './widgets/current-time-widget',
    './widgets/results-list-widget',
    './widgets/video-widget',
    './widgets/trending-widget'
], function(StaticContentWidget, StaticImageWidget, MapWidget, SunburstWidget, TopicMapWidget,
            TimeLastRefreshedWidget, CurrentTimeWidget, ResultsListWidget, VideoWidget, TrendingWidget) {
    'use strict';

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

    return function(widget) {
        return registry[widget];
    }
});
