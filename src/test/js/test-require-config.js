/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

require.config({
    baseUrl: 'src/main/webapp/static/js',
    paths: {
        'js-testing': '../lib/hp-autonomy-js-testing-utils/src/js',
        'mock': '../../../../test/js/mock'
    },
    map: {
        '*': {
            'find/lib/backbone/backbone-extensions': 'backbone'
        },
        'find/app/page/service-view': {
            'find/app/model/indexes-collection': 'mock/model/indexes-collection'
        }
    }
});
