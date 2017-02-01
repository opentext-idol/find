/*
 * Copyright 2014-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    './widgets/static-content',
    './widgets/static-image-widget',
    './widgets/map-widget',
    './widgets/topic-map-widget',
    './widgets/time-last-refreshed-widget',
    './widgets/current-time',
    './widgets/video-widget'
], function(StaticContentWidget, StaticImageWidget, MapWidget, TopicMapWidget, TimeLastRefreshedWidget, CurrentTime, VideoWidget) {
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
        videoWidget: {
            Constructor: VideoWidget
        }
    };

    return function(widget) {
        return registry[widget];
    }

});
