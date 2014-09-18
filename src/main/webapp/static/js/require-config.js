require.config({
    paths: {
        'about-page': '../lib/about-page',
        backbone: 'find/lib/backbone/backbone-extensions',
        'backbone-base': '../lib/backbone/backbone',
        bootstrap: '../lib/bootstrap/js/bootstrap',
        colorbox: '../lib/colorbox/jquery.colorbox',
        'config-wizard': '../lib/config-wizard',
        i18n: '../lib/require/i18n',
        jquery: '../lib/jquery/jquery',
        'js-utils': '../lib/javascript-utils',
        json2: '../lib/json/json2',
        'login-page': '../lib/login-page',
        settings: '../lib/settings',
        store: '../lib/store/store',
        text: '../lib/require/text',
        underscore: '../lib/underscore/underscore'
    },
    shim: {
        'backbone-base': {
            deps: ['underscore', 'jquery', 'json2'],
            exports: 'Backbone'
        },
        bootstrap: ['jquery'],
        colorbox: ['jquery'],
        underscore: {
            exports: '_'
        }
    }
});