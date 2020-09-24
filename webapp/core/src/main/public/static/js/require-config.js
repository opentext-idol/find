/*
 * Copyright 2014-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

require.config({
    paths: {
        css: '../css',
        'about-page': '../bower_components/hp-autonomy-about-page/src',
        backbone: '../bower_components/backbone/backbone',
        bootstrap: '../bower_components/bootstrap/dist/js/bootstrap',
        bowser: '../bower_components/bjoska.bowser/bowser',
        'bootstrap-datetimepicker': '../bower_components/eonasdan-bootstrap-datetimepicker/src/js/bootstrap-datetimepicker',
        'databases-view': '../bower_components/hp-autonomy-js-databases-view/src',
        'datatables.net': '../bower_components/datatables.net/js/jquery.dataTables',
        'datatables.net-bs': '../bower_components/datatables.net-bs/js/dataTables.bootstrap',
        'datatables.net-fixedColumns': '../bower_components/datatables.net-fixedcolumns/js/dataTables.fixedColumns',
        'dropzone': '../bower_components/dropzone/dist/dropzone-amd-module',
        'd3': '../bower_components/d3/d3',
        'fieldtext/js/parser': 'pegjs/fieldtext/parser',
        'idol-wkt/js/parser': 'pegjs/idol-wkt/parser',
        handlebars: '../bower_components/handlebars/handlebars',
        'html2canvas': '../bower_components/html2canvas/build/html2canvas',
        i18n: '../bower_components/requirejs-i18n/i18n',
        'flot': '../bower_components/flot/jquery.flot',
        'flot.time': '../bower_components/flot/jquery.flot.time',
        'flot.stack': '../bower_components/flot/jquery.flot.stack',
        'flot.categories': '../bower_components/flot/jquery.flot.categories',
        'fieldtext': '../bower_components/hp-autonomy-fieldtext-js/src',
        'parametric-refinement': '../bower_components/hp-autonomy-js-parametric-refinement/src',
        iCheck: '../bower_components/iCheck/icheck',
        chosen: '../bower_components/chosen/chosen.jquery',
        metisMenu: '../bower_components/metisMenu/dist/metisMenu',
        jquery: '../bower_components/jquery/jquery',
        'js-whatever': '../bower_components/hp-autonomy-js-whatever/src',
        json2: '../bower_components/json/json2',
        'login-page': '../bower_components/hp-autonomy-login-page/src',
        leaflet: '../bower_components/leaflet/dist/leaflet-src',
        'Leaflet.awesome-markers': '../bower_components/Leaflet.awesome-markers/dist/leaflet.awesome-markers',
        'leaflet.draw': '../bower_components/leaflet-draw/dist/leaflet.draw-src',
        'leaflet.draw.i18n': 'leaflet.draw.i18n/leaflet.draw.i18n',
        'leaflet.draw.negate': 'leaflet.draw.negate/leaflet.draw.negate',
        'leaflet.draw.polygonSpatial': 'leaflet.draw.polygonSpatial/leaflet.draw.polygonSpatial',
        'leaflet.markercluster': '../bower_components/leaflet.markercluster/dist/leaflet.markercluster-src',
        'leaflet.markercluster.layersupport': '../bower_components/leaflet.markercluster.layersupport/src/layersupport',
        'leaflet.notransform': 'leaflet.notransform/leaflet.notransform',
        moment: '../bower_components/moment/moment',
        'moment-timezone-with-data': '../bower_components/moment-timezone/builds/moment-timezone-with-data',
        Raphael: '../bower_components/raphael/raphael',
        settings: '../bower_components/hp-autonomy-settings-page/src',
        slider: '../bower_components/seiyria-bootstrap-slider/dist',
        text: '../bower_components/requirejs-text/text',
        sunburst: '../bower_components/hp-autonomy-sunburst/src',
        topicmap: '../bower_components/hp-autonomy-topic-map/src',
        underscore: '../bower_components/underscore/underscore',
        typeahead: '../bower_components/corejs-typeahead/dist/typeahead.jquery',
        uuidjs: '../bower_components/uuid.js/src/uuid'
    },
    shim: {
        'backbone': {
            exports: 'Backbone'
        },
        bootstrap: ['jquery'],
        'bootstrap-datetimepicker': ['jquery'],
        chosen: ['jquery'],
        d3: {
            exports: 'd3'
        },
        flot: ['jquery'],
        'flot.time': ['flot'],
        // You have to load the stack plugin after the categories plugin
        // https://github.com/flot/flot/issues/1042
        'flot.stack': ['flot', 'flot.categories'],
        'flot.categories': ['flot'],
        html2canvas: {
            exports: 'html2canvas'
        },
        iCheck: ['jquery'],
        underscore: {
            exports: '_'
        },
        // This isn't a real dependency, but just makes sure that the negate button is above the polygonSpatial one
        //   since the last to load is closest to the top.
        'leaflet.draw.negate': ['leaflet.draw.polygonSpatial'],
        'Leaflet.awesome-markers': ['leaflet'],
        'leaflet.draw': ['leaflet'],
        'leaflet.markercluster': ['leaflet'],
        'leaflet.markercluster.layersupport': ['leaflet.markercluster'],
        'leaflet': ['leaflet.notransform'],
        uuidjs: {
            exports: 'UUID'
        }
    }
});
