'use strict';

const path = require('path');
const CopyWebpackPlugin = require('copy-webpack-plugin');

const MERGED_JS = path.resolve(__dirname, 'target/classes/static/js');
const VENDOR_MODULES = path.resolve(__dirname, '../core/frontend/node_modules');
const SHIMS = path.resolve(__dirname, 'build/vendor-shims');

module.exports = (env, argv) => {
    const isProduction = argv.mode === 'production';

    return {
        mode: isProduction ? 'production' : 'development',
        devtool: isProduction ? 'source-map' : 'eval-source-map',
        entry: {
            public: path.resolve(MERGED_JS, 'public.js'),
            config: path.resolve(MERGED_JS, 'config.js'),
            login: path.resolve(MERGED_JS, 'login.js'),
            themetracker: path.resolve(MERGED_JS, 'themetracker.js')
        },
        output: {
            path: path.resolve(MERGED_JS, 'bundle'),
            filename: '[name].js',
            clean: true
        },
        resolve: {
            modules: [MERGED_JS, VENDOR_MODULES, 'node_modules'],
            extensions: ['.js'],
            mainFields: ['browser', 'main'],
            alias: {
                // PEG.js-generated parsers (built by core/build/peg.js).
                'fieldtext/js/parser$': path.resolve(MERGED_JS, 'pegjs/fieldtext/parser'),
                'idol-wkt/js/parser$': path.resolve(MERGED_JS, 'pegjs/idol-wkt/parser'),

                // leaflet plugin files that live in a same-named directory with no index.js/package.json, so plain
                // directory resolution would fail.
                'leaflet.draw.i18n$': path.resolve(MERGED_JS, 'leaflet.draw.i18n/leaflet.draw.i18n'),
                'leaflet.draw.negate$': path.resolve(MERGED_JS, 'leaflet.draw.negate/leaflet.draw.negate'),
                'leaflet.draw.polygonSpatial$': path.resolve(MERGED_JS, 'leaflet.draw.polygonSpatial/leaflet.draw.polygonSpatial'),
                'leaflet.notransform$': path.resolve(MERGED_JS, 'leaflet.notransform/leaflet.notransform'),

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
                'about-page': path.resolve(VENDOR_MODULES, 'hp-autonomy-about-page/src'),
                'databases-view': path.resolve(VENDOR_MODULES, 'hp-autonomy-js-databases-view/src'),
                'fieldtext': path.resolve(VENDOR_MODULES, 'hp-autonomy-fieldtext-js/src'),
                'parametric-refinement': path.resolve(VENDOR_MODULES, 'hp-autonomy-js-parametric-refinement/src'),
                'js-whatever': path.resolve(VENDOR_MODULES, 'hp-autonomy-js-whatever/src'),
                'login-page': path.resolve(VENDOR_MODULES, 'hp-autonomy-login-page/src'),
                'settings': path.resolve(VENDOR_MODULES, 'hp-autonomy-settings-page/src'),
                'slider': path.resolve(VENDOR_MODULES, 'bootstrap-slider/dist'),
                'sunburst': path.resolve(VENDOR_MODULES, 'hp-autonomy-sunburst/src'),
                'topicmap': path.resolve(VENDOR_MODULES, 'hp-autonomy-topic-map/src'),

                // renames
                'bootstrap-datetimepicker$': path.resolve(VENDOR_MODULES, 'eonasdan-bootstrap-datetimepicker'),
                'datatables.net-fixedColumns$': path.resolve(VENDOR_MODULES, 'datatables.net-fixedcolumns'),
                'dropzone$': path.resolve(VENDOR_MODULES, 'dropzone/dist/dropzone-amd-module'),
                'handlebars$': path.resolve(VENDOR_MODULES, 'handlebars/dist/handlebars.js'),
                'qs$': path.resolve(VENDOR_MODULES, 'qs/dist/qs'),
                'metisMenu$': path.resolve(VENDOR_MODULES, 'metismenu'),
                'moment$': path.resolve(VENDOR_MODULES, 'moment/min/moment-with-locales'),
                'moment-timezone-with-data$': path.resolve(VENDOR_MODULES, 'moment-timezone/builds/moment-timezone-with-data'),
                'Raphael$': path.resolve(VENDOR_MODULES, 'raphael/raphael.js'),
                'typeahead$': path.resolve(VENDOR_MODULES, 'corejs-typeahead/dist/typeahead.jquery'),
                // this fixes `_.partial(fn, _, x)`
                'underscore$': path.resolve(VENDOR_MODULES, 'underscore/underscore.js')
            }
        },
        module: {
            rules: [
                {
                    test: /\.(html|handlebars)$/,
                    loader: path.resolve(__dirname, 'build/loaders/raw-text-loader.js')
                },
                {
                    test: /\.js$/,
                    include: MERGED_JS,
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
            }),
            new (require('webpack').NormalModuleReplacementPlugin)(/^text!/, (resource) => {
                resource.request = resource.request.replace(/^text!/, '');
            }),
            new (require('webpack').NormalModuleReplacementPlugin)(/^i18n!/, (resource) => {
                resource.request = resource.request.replace(/^i18n!/, '');
            }),
            ...(isProduction ? [] : [
                new CopyWebpackPlugin({
                    patterns: [
                        { from: '../core/src/main/public/static/find-favicon.ico', to: 'find-favicon.ico' },
                        { from: '../core/src/main/public/static/fonts', to: 'fonts' },
                        { from: '../core/src/main/public/static/img', to: 'img' },
                        { from: 'src/main/public/static/img', to: 'img' },
                        { from: 'src/main/public/static/css', to: 'css' },
                        { from: 'src/main/public/static/html', to: 'html' },
                        { from: 'src/main/public/static/js', to: MERGED_JS }
                    ].map((pattern) => ({ ...pattern, context: __dirname, noErrorOnMissing: true }))
                })
            ])
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
