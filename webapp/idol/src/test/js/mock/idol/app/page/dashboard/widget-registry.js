/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/idol/app/page/dashboard/widgets/widget',
    'find/idol/app/page/dashboard/widgets/updating-widget'
], function(Widget, UpdatingWidget) {
    'use strict';

    const registry = {
        widget: {
            Constructor: Widget
        },
        updatingWidget: {
            Constructor: UpdatingWidget
        }
    };

    return function(widget) {
        return registry[widget];
    }
});
