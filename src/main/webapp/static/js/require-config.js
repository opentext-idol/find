require.config({
    paths: {
        'about-page': '../lib/about-page',
        backbone: 'find/lib/backbone/backbone-extensions',
        'backbone-base': '../lib/backbone/backbone',
        bootstrap: '../lib/bootstrap/js/bootstrap',
        colorbox: '../lib/colorbox/js/jquery.colorbox',
        'config-wizard': '../lib/config-wizard',
        datatables: '../lib/datatables/js/datatables.bootstrap',
        datatables_base: '../lib/datatables/js/jquery.dataTables',
        'datatables-plugins': '../lib/datatables/js',
        i18n: '../lib/require/i18n',
        jmousewheel: '../lib/jquery-mousewheel/js/jquery.mousewheel',
        jquery: '../lib/jquery/jquery',
        jqueryTree: '../lib/jqtree/js/tree.jquery',
        'js-utils': '../lib/javascript-utils',
        json2: '../lib/json/json2',
        'login-page': '../lib/login-page',
        polyfill: '../lib/polyfill/polyfill',
        raphael : '../lib/raphael/raphael',
        scrollNearEnd : '../lib/scroll-near-end/scroll-near-end',
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
        jmousewheel: ['jquery'],
        jqueryTree: ['jquery'],
        scrollNearEnd: ['jquery'],
        underscore: {
            exports: '_'
        }
    }
});