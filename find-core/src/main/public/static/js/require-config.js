/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

require.config({
    paths: {
        'about-page': '../bower_components/hp-autonomy-about-page/src',
        backbone: 'find/lib/backbone/backbone-extensions',
        'backbone-base': '../bower_components/backbone/backbone',
        bootstrap: '../lib/bootstrap/js/bootstrap',
        'bootstrap-datetimepicker': '../bower_components/eonasdan-bootstrap-datetimepicker/src/js/bootstrap-datetimepicker',
        'databases-view': '../bower_components/hp-autonomy-js-databases-view/src',
        'datatables.net': '../bower_components/datatables.net/js/jquery.dataTables',
        'datatables.net-bs': '../bower_components/datatables.net-bs/js/dataTables.bootstrap',
        'd3': '../bower_components/d3/d3',
        i18n: '../bower_components/requirejs-i18n/i18n',
        'peg': '../bower_components/pegjs/peg-0.8.0',
        'fieldtext': '../bower_components/hp-autonomy-fieldtext-js/src',
        'parametric-refinement': '../bower_components/hp-autonomy-js-parametric-refinement/src',
        iCheck: '../bower_components/iCheck/icheck',
        chosen: '../bower_components/chosen/chosen.jquery',
        jquery: '../bower_components/jquery/jquery',
        'js-whatever': '../bower_components/hp-autonomy-js-whatever/src',
        json2: '../bower_components/json/json2',
        'login-page': '../bower_components/hp-autonomy-login-page/src',
        leaflet: '../bower_components/leaflet/dist/leaflet-src',
        moment: '../bower_components/moment/moment',
        raphael: '../bower_components/raphael/raphael',
        settings: '../bower_components/hp-autonomy-settings-page/src',
        slider: '../bower_components/seiyria-bootstrap-slider/dist',
        text: '../bower_components/requirejs-text/text',
        sunburst: '../bower_components/hp-autonomy-sunburst/src',
        topicmap: '../bower_components/hp-autonomy-topic-map/src',
        underscore: '../bower_components/underscore/underscore',
        typeahead: '../bower_components/corejs-typeahead/dist/typeahead.jquery'
    },
    shim: {
        'backbone-base': {
            exports: 'Backbone'
        },
        bootstrap: ['jquery'],
        'bootstrap-datetimepicker': ['jquery'],
        chosen: ['jquery'],
        iCheck: ['jquery'],
        peg: {
            exports: 'PEG'
        },
        underscore: {
            exports: '_'
        }
    }
});
