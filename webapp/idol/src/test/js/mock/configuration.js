/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([], function () {
    var spy = jasmine.createSpy('configuration');
    spy.and.returnValue({
        map: {
            enabled: true,
            resultsStep: 2500,
            locationFields: [{
                displayName: 'test',
                latitudeField: 'LATITUDE',
                longitudeField: 'LONGITUDE'
            }]
        }
    });
    return spy;
});