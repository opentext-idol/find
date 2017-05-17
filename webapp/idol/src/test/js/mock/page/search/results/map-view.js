/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-testing/backbone-mock-factory'
], function(mockFactory) {
    'use strict';

    const getMarkerSpy = jasmine.createSpy('getMarker').and.returnValue('testMarker');
    const mapRenderedSpy = jasmine.createSpy('mapRendered').and.returnValue(true);
    return mockFactory.getView(['getDivIconCreateFunction', 'getIcon', 'fitMapToMarkerBounds', 'addClusterLayer', 'clearMarkers', 'addGroupingLayer', 'addMarkers'], {
        getMarker: getMarkerSpy,
        mapRendered: mapRenderedSpy
    });
});
