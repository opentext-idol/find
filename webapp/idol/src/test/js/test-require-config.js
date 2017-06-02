/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

require.config({
    baseUrl: 'target/classes/static/js',
    paths: {
        'jasmine-ajax': '../bower_components/jasmine-ajax/lib/mock-ajax',
        'jasmine-jquery': '../bower_components/jasmine-jquery/lib/jasmine-jquery',
        'js-testing': '../bower_components/hp-autonomy-js-testing-utils/src/js',
        'mock': '../../../../src/test/js/mock'
    },
    shim: {
        'jasmine-jquery': ['jquery']
    },
    map: {
        '*': {
            'html2canvas': 'mock/html2canvas',
            'find/app/configuration': 'mock/configuration',
            'find/lib/backbone/backbone-extensions': 'backbone',
            'find/idol/app/model/comparison/comparison-model': 'mock/idol/model/comparison/comparison-model',
            'find/app/page/search/results/map-view': 'mock/page/search/results/map-view',
            'find/idol/app/model/answer-bank/idol-answered-questions-collection': 'mock/idol/model/answer-bank/idol-answered-questions-collection',
            'find/idol/app/model/comparison/comparison-documents-collection': 'mock/idol/app/model/comparison/comparison-documents-collection',
            'find/idol/app/page/dashboard/widget-registry': 'mock/idol/app/page/dashboard/widget-registry'
        }
    }
});
