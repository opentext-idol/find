/*
 * Copyright 2014-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    './widgets/static-content',
    './widgets/map-widget',
    './widgets/topic-map-widget',
    './widgets/time-last-refreshed-widget'
], function(StaticContentWidget, MapWidget, TopicMapWidget, TimeLastRefreshedWidget) {
    'use strict';

    const registry = {
        staticContentWidget: {
            Constructor: StaticContentWidget
        },
        mapWidget: {
            Constructor: MapWidget
        },
        topicMapWidget: {
            Constructor: TopicMapWidget
        },
        timeLastRefreshedWidget: {
            Constructor: TimeLastRefreshedWidget
        }
    };

    return function(widget) {
        return registry[widget];
    }

});
