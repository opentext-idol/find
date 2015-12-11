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
            'find/lib/backbone/backbone-extensions': 'backbone'
        },
        'find/app/page/service-view': {
            'find/app/model/indexes-collection': 'mock/model/indexes-collection'
        },
        'find/app/page/related-concepts/related-concepts-view': {
            'find/app/model/documents-collection': 'mock/model/documents-collection'
        }
    }
});
