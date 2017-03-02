/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    './widgets/static-content',
    './widgets/static-image-widget',
    './widgets/map-widget',
    './widgets/topic-map-widget',
    './widgets/time-last-refreshed-widget',
    './widgets/current-time',
    './widgets/results-list-widget',
    './widgets/video-widget'
], function(StaticContentWidget, StaticImageWidget, MapWidget, TopicMapWidget, TimeLastRefreshedWidget,
            CurrentTime, ResultsListWidget, VideoWidget) {
    'use strict';

    const registry = {
        staticContentWidget: {
            Constructor: StaticContentWidget
        },
        staticImageWidget: {
            Constructor: StaticImageWidget
        },
        mapWidget: {
            Constructor: MapWidget
        },
        topicMapWidget: {
            Constructor: TopicMapWidget
        },
        timeLastRefreshedWidget: {
            Constructor: TimeLastRefreshedWidget
        },
        currentTimeWidget: {
            Constructor: CurrentTime
        },
        resultsListWidget: {
            Constructor: ResultsListWidget
        },
        videoWidget: {
            Constructor: VideoWidget
        }
    };

    return function(widget) {
        return registry[widget];
    }
});
