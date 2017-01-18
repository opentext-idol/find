/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-testing/backbone-mock-factory'
], function(mockFactory) {
    'use strict';

    var layer = jasmine.createSpyObj('layer', ['addLayer', 'clearLayers', 'getLayers']);
    var createLayerSpy = jasmine.createSpy('createLayer').and.returnValue(layer);
    var getMarkerSpy = jasmine.createSpy('getMarker').and.returnValue('testMarker');
    return mockFactory.getView(['getDivIconCreateFunction', 'getIcon', 'loaded', 'addLayer'], {
        createLayer: createLayerSpy,
        getMarker: getMarkerSpy
    });
});
