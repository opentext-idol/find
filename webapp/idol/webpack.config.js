'use strict';

const path = require('path');

const INPUTS = path.resolve(__dirname, 'target/frontend/static');
const LIB = path.resolve(INPUTS, 'lib');
const CORE_JS = path.resolve(INPUTS, 'js');
// listed ahead of CORE_JS below, so idol's modules override core's same-named ones
const IDOL_JS = path.resolve(__dirname, 'src/main/public/static/js');
const SHIMS = path.resolve(__dirname, 'build/vendor-shims');
const BUNDLE = path.resolve(__dirname, 'target/classes/static/js/bundle');

module.exports = (env, argv) => {
    const production = argv.mode === 'production';

    return {
        mode: production ? 'production' : 'development',
        devtool: production ? false : 'eval-source-map',
        entry: {
            public: path.resolve(IDOL_JS, 'public.js'),
            config: path.resolve(CORE_JS, 'config.js'),
            login: path.resolve(CORE_JS, 'login.js'),
            themetracker: path.resolve(IDOL_JS, 'themetracker.js')
        },
        output: {
            path: BUNDLE,
            filename: '[name].js',
            clean: true
        },
        resolve: {
            modules: ['node_modules', LIB, IDOL_JS, CORE_JS],
            extensions: ['.js'],
            mainFields: ['browser', 'main'],
            alias: {
                // PEG.js-generated parsers (built by frontend/build/peg.js).
                'idol-wkt/js/parser$': path.resolve(CORE_JS, 'pegjs/idol-wkt/parser'),

                // leaflet plugin files that live in a same-named directory with no index.js/package.json, so plain
                // directory resolution would fail.
                'leaflet.draw.i18n$': path.resolve(CORE_JS, 'leaflet.draw.i18n/leaflet.draw.i18n'),
                'leaflet.draw.negate$': path.resolve(CORE_JS, 'leaflet.draw.negate/leaflet.draw.negate'),
                'leaflet.draw.polygonSpatial$': path.resolve(CORE_JS, 'leaflet.draw.polygonSpatial/leaflet.draw.polygonSpatial'),
                'leaflet.notransform$': path.resolve(CORE_JS, 'leaflet.notransform/leaflet.notransform'),

                // shims, for libraries that don't ship their own UMD/CJS wrapper
                'jquery$': path.resolve(SHIMS, 'jquery.js'),
                'bootstrap$': path.resolve(SHIMS, 'bootstrap.js'),
                'iCheck$': path.resolve(SHIMS, 'iCheck.js'),
                'chosen$': path.resolve(SHIMS, 'chosen.js'),
                'flot$': path.resolve(SHIMS, 'flot.js'),
                'flot.time$': path.resolve(SHIMS, 'flot.time.js'),
                'flot.categories$': path.resolve(SHIMS, 'flot.categories.js'),
                'flot.stack$': path.resolve(SHIMS, 'flot.stack.js'),
                'html2canvas$': path.resolve(SHIMS, 'html2canvas.js'),
                'leaflet$': path.resolve(SHIMS, 'leaflet.js'),
                'leaflet.draw$': path.resolve(SHIMS, 'leaflet.draw.js'),
                'Leaflet.awesome-markers$': path.resolve(SHIMS, 'Leaflet.awesome-markers.js'),
                'leaflet.markercluster$': path.resolve(SHIMS, 'leaflet.markercluster.js'),

                // subdirectories
                'about-page': path.resolve(LIB, 'hp-autonomy-about-page/src'),
                'databases-view': path.resolve(LIB, 'hp-autonomy-js-databases-view/src'),
                'fieldtext': path.resolve(LIB, 'hp-autonomy-fieldtext-js/src'),
                'parametric-refinement': path.resolve(LIB, 'hp-autonomy-js-parametric-refinement/src'),
                'js-whatever': path.resolve(LIB, 'hp-autonomy-js-whatever/src'),
                'login-page': path.resolve(LIB, 'hp-autonomy-login-page/src'),
                'settings': path.resolve(LIB, 'hp-autonomy-settings-page/src'),
                'slider': path.resolve(LIB, 'bootstrap-slider/dist'),
                'sunburst': path.resolve(LIB, 'hp-autonomy-sunburst/src'),
                'topicmap': path.resolve(LIB, 'hp-autonomy-topic-map/src'),

                // renames
                'bootstrap-datetimepicker$': path.resolve(LIB, 'eonasdan-bootstrap-datetimepicker'),
                'datatables.net-fixedColumns$': path.resolve(LIB, 'datatables.net-fixedcolumns'),
                'dropzone$': path.resolve(LIB, 'dropzone/dist/dropzone-amd-module'),
                'handlebars$': path.resolve(LIB, 'handlebars/dist/handlebars.js'),
                'qs$': path.resolve(LIB, 'qs/dist/qs'),
                'metisMenu$': path.resolve(LIB, 'metismenu'),
                'moment$': path.resolve(LIB, 'moment/min/moment-with-locales'),
                'moment-timezone-with-data$': path.resolve(LIB, 'moment-timezone/builds/moment-timezone-with-data'),
                'Raphael$': path.resolve(LIB, 'raphael/raphael.js'),
                'typeahead$': path.resolve(LIB, 'corejs-typeahead/dist/typeahead.jquery'),
                // this fixes `_.partial(fn, _, x)`
                'underscore$': path.resolve(LIB, 'underscore/underscore.js')
            }
        },
        module: {
            rules: [
                {
                    test: /\.(html|handlebars)$/,
                    type: 'javascript/auto',
                    loader: path.resolve(__dirname, 'build/loaders/raw-text-loader.js')
                },
                {
                    test: /\.js$/,
                    include: [IDOL_JS, CORE_JS],
                    parser: {
                        amd: false
                    }
                }
            ]
        },
        plugins: [
            new (require('webpack').IgnorePlugin)({
                resourceRegExp: /^\.\/locale$/,
                contextRegExp: /moment[/\\]min$/
            })
        ],
        optimization: {
            splitChunks: false,
            runtimeChunk: false
        },
        performance: {
            hints: false
        },
        cache: {
            type: 'filesystem',
            cacheDirectory: path.resolve(__dirname, 'target/webpack-cache')
        },
        stats: {
            errorDetails: true
        }
    };
};
