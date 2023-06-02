/*
 * Copyright 2016-2017 Open Text.
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
