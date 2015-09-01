/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

require.config({
    paths: {
        backbone: 'find/lib/backbone/backbone-extensions',
        'backbone-base': '../bower_components/backbone/backbone',
        bootstrap: '../lib/bootstrap/js/bootstrap',
        colorbox: '../bower_components/colorbox/jquery.colorbox',
        'bootstrap-datetimepicker': '../bower_components/eonasdan-bootstrap-datetimepicker/src/js/bootstrap-datetimepicker',
        'databases-view': '../bower_components/hp-autonomy-js-databases-view/src',
        i18n: '../bower_components/requirejs-i18n/i18n',
        iCheck: '../bower_components/icheck/icheck',
        'peg': '../bower_components/pegjs/peg-0.8.0',
        'fieldtext': '../bower_components/hp-autonomy-fieldtext-js/src',
        'parametric-refinement': '../bower_components/hp-autonomy-js-parametric-refinement/src',
        jquery: '../bower_components/jquery/jquery',
        'js-whatever': '../bower_components/hp-autonomy-js-whatever/src',
        json2: '../bower_components/json/json2',
        'login-page': '../bower_components/hp-autonomy-login-page/src',
        moment: '../bower_components/moment/moment',
        settings: '../bower_components/hp-autonomy-settings-page/src',
        text: '../bower_components/requirejs-text/text',
        underscore: '../bower_components/underscore/underscore'
    },
    shim: {
        'backbone-base': {
            deps: ['underscore', 'jquery', 'json2'],
            exports: 'Backbone'
        },
        bootstrap: ['jquery'],
        'bootstrap-datetimepicker': ['jquery'],
        colorbox: ['jquery'],
        iCheck: ['jquery'],
        peg: {
            exports: 'PEG'
        },
        underscore: {
            exports: '_'
        }
    }
});
