/*
 * Copyright 2016-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([], function() {
    'use strict';

    var spy = jasmine.createSpy('configuration');
    spy.and.returnValue({
        answerServerEnabled: true,
        assetsConfig: {
            assets: {
                'BIG_LOGO': 'foo.png'
            }
        },
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
