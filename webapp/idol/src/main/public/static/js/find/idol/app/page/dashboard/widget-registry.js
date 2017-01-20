/*
 * Copyright 2014-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    './widgets/static-content'
], function(StaticContentWidget) {
    'use strict';

    const registry = {
        staticContentWidget: {
            Constructor: StaticContentWidget
        }
    };

    return function(widget) {
        return registry[widget];
    }

});