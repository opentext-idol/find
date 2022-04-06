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
