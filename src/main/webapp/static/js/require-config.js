/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

require.config({
    paths: {
        backbone: 'find/lib/backbone/backbone-extensions',
        'backbone-base': '../lib/backbone/backbone',
        bootstrap: '../lib/hp-autonomy-bootstrap-2/bootstrap/js/bootstrap',
        colorbox: '../lib/colorbox/jquery.colorbox',
        'bootstrap-datetimepicker': '../lib/smalot-bootstrap-datetimepicker/js/bootstrap-datetimepicker',
        i18n: '../lib/requirejs-i18n/i18n',
        iCheck: '../lib/icheck/icheck',
        'peg': '../lib/pegjs/peg-0.8.0',
        'fieldtext': '../lib/hp-autonomy-fieldtext-js/src',
        jquery: '../lib/jquery/jquery',
        'js-whatever': '../lib/hp-autonomy-js-whatever/src',
        json2: '../lib/json/json2',
        'login-page': '../lib/hp-autonomy-login-page/src',
        moment: '../lib/moment/moment',
        settings: '../lib/hp-autonomy-settings-page/src',
        text: '../lib/requirejs-text/text',
        underscore: '../lib/underscore/underscore'
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
