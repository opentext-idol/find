/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define(function() {
    'use strict';

    const MockTrending = function() {
        MockTrending.instances.push(this);
        this.draw = jasmine.createSpy('draw');
    };

    MockTrending.reset = function() {
        MockTrending.instances = [];
    };

    MockTrending.reset();
    return MockTrending;
});
