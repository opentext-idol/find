/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

require.config({
    baseUrl: 'src/main/public/static/js',
    paths: {
        'jasmine-jquery': '../bower_components/jasmine-jquery/lib/jasmine-jquery',
        'js-testing': '../bower_components/hp-autonomy-js-testing-utils/src/js',
        'mock': '../../../../test/js/mock'
    },
    shim: {
        'jasmine-jquery': ['jquery']
    },
    map: {
        '*': {
            'find/app/configuration': 'mock/configuration',
            'find/app/router': 'mock/router',
            'find/app/vent': 'mock/vent',
            'find/app/util/database-name-resolver': 'mock/database-name-resolver',
            'find/lib/backbone/backbone-extensions': 'backbone',
            'find/app/util/confirm-view': 'mock/util/confirm-view',
            'find/app/model/comparisons/comparison-model': 'mock/model/comparisons/comparison-model',
            'find/app/page/search/document/location-tab': 'mock/page/search/document/location-tab',
            'find/app/util/topic-map-view': 'mock/util/topic-map-view'
        },
        'find/app/page/search/service-view': {
            'find/app/model/indexes-collection': 'mock/model/indexes-collection'
        },
        'find/app/page/search/related-concepts/related-concepts-view': {
            'find/app/model/documents-collection': 'mock/model/documents-collection'
        }
    }
});
