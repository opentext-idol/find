/*
 * Copyright 2014-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

require.config({
    baseUrl: 'src/main/public/static/js',
    paths: {
        'jasmine-ajax': '../bower_components/jasmine-ajax/lib/mock-ajax',
        'jasmine-jquery': '../bower_components/jasmine-jquery/lib/jasmine-jquery',
        'js-testing': '../bower_components/hp-autonomy-js-testing-utils/src/js',
        'mock': '../../../../test/js/mock',
        'test-util': '../../../../test/js/util',
        'resources': '../../../../test/js/resources',
        'fieldtext/js/parser': '../../../../../target/classes/static/js/pegjs/fieldtext/parser',
        'idol-wkt/js/parser': '../../../../../target/classes/static/js/pegjs/idol-wkt/parser'
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
            'find/app/util/confirm-view': 'mock/util/confirm-view',
            'find/app/page/search/document/location-tab': 'mock/page/search/document/location-tab',
            'find/app/util/topic-map-view': 'mock/util/topic-map-view',
            'find/app/model/bucketed-numeric-collection': 'mock/model/bucketed-numeric-collection',
            'find/app/model/bucketed-date-collection': 'mock/model/bucketed-date-collection',
            'find/app/model/parametric-collection': 'mock/model/parametric-collection',
            'find/app/model/date-field-details-model': 'mock/model/parametric-details-model'
        },
        'find/app/page/search/related-concepts/related-concepts-view': {
            'find/app/model/documents-collection': 'mock/model/documents-collection'
        },
        'find/app/page/search/results/parametric-results-view': {
            'find/app/model/dependent-parametric-collection': 'mock/model/dependent-parametric-collection'
        },
        'find/app/page/search/filter-view': {
            'find/app/configuration': 'mock/configuration',
            'find/app/page/search/filters/date/dates-filter-view': 'mock/page/search/filters/date/dates-filter-view',
            'find/app/page/search/filters/parametric/numeric-parametric-view': 'mock/page/search/filters/parametric/numeric-parametric-view',
            'find/app/page/search/filters/parametric/parametric-view': 'mock/page/search/filters/parametric/parametric-view',
            'parametric-refinement/display-collection': 'mock/model/display-collection'
        },
        'find/app/page/search/results/entity-topic-map-view': {
            'find/app/model/entity-collection': 'mock/model/entity-collection',
            'find/app/util/range-input': 'mock/util/range-input'
        },
        'find/app/page/search/filters/parametric/parametric-select-modal-view': {
            'find/app/page/search/filters/parametric/parametric-paginator': 'mock/page/search/filters/parametric/parametric-paginator'
        },
        'find/app/page/search/filters/parametric/parametric-select-modal': {
            'find/app/page/search/filters/parametric/parametric-paginator': 'mock/page/search/filters/parametric/parametric-paginator'
        },
        'find/app/page/search/results/trending/trending-view': {
            'find/app/page/search/results/trending/trending-strategy': 'mock/page/results/trending-strategy',
            'find/app/page/search/results/trending/trending': 'mock/page/results/trending',
            'find/app/util/range-input': 'mock/util/range-input'
        },
        'find/app/page/search/document-content-view': {
            'find/app/model/document-model': 'mock/model/document-model'
        },
        'find/app/page/search/saved-searches/saved-search-control-view': {
            'find/app/util/modal': 'mock/util/modal',
            'find/app/util/policy-selection-view': 'mock/util/policy-selection-view'
        }
    }
});
