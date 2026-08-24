'use strict';

/*
 * webpack config for the idol module's 4 browser bundles (public, config, login,
 * themetracker) - see amd-to-cjs-find.md. Sources are still AMD (`define()`/`require()`
 * with callback factories); webpack understands that natively, so nothing here needs to
 * wait for the Phase 7 codemod that converts them to CommonJS.
 *
 * It runs against the *merged* static/js tree that Maven's unpack-dependencies execution
 * produces at target/classes/static/js (core's config.js/login.js copied in alongside
 * idol's public.js/themetracker.js) - core's overWriteIfNewer is now `true`, so a plain
 * `mvn install -pl core` (no idol `clean`) is enough to propagate core JS changes here.
 * Vendor libraries resolve directly from core/frontend/node_modules rather than the
 * bower_components copies, since those copies are just an npm->bower_components sync of
 * the same packages (see docs/_building/Understanding-the-Code-Structure.md).
 */

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
            // Group 1: PEG.js-generated parsers (built by core/build/peg.js, Phase 1).
            'fieldtext/js/parser$': path.resolve(MERGED_JS, 'pegjs/fieldtext/parser'),
            'idol-wkt/js/parser$': path.resolve(MERGED_JS, 'pegjs/idol-wkt/parser'),

            // Group 2: leaflet plugin files that live in a same-named directory with no
            // index.js/package.json, so plain directory resolution would fail.
            'leaflet.draw.i18n$': path.resolve(MERGED_JS, 'leaflet.draw.i18n/leaflet.draw.i18n'),
            'leaflet.draw.negate$': path.resolve(MERGED_JS, 'leaflet.draw.negate/leaflet.draw.negate'),
            'leaflet.draw.polygonSpatial$': path.resolve(MERGED_JS, 'leaflet.draw.polygonSpatial/leaflet.draw.polygonSpatial'),
            'leaflet.notransform$': path.resolve(MERGED_JS, 'leaflet.notransform/leaflet.notransform'),

            // Group 3: vendor shims. These replicate the RequireJS `shim` config (dependency
            // ordering + global exports) for libraries that don't ship their own UMD/CJS
            // wrapper. The `$` suffix makes each alias match only the exact bare id, so the
            // shim itself can `require()` the real underlying package without recursing.
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

            // Group 4: hp-autonomy-* vendor libraries distributed as source directories
            // (no build step of their own) plus bootstrap-slider's dist folder. No `$`
            // suffix here - these ids are always used with a subpath (e.g.
            // 'js-whatever/js/substitution'), so the alias must act as a directory prefix.
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

            // Group 5: real npm package names/subpaths differ from the short ids used
            // throughout the source (case differences, or a specific browser/full build
            // instead of the package's default `main`).
            'bootstrap-datetimepicker$': path.resolve(VENDOR_MODULES, 'eonasdan-bootstrap-datetimepicker'),
            'datatables.net-fixedColumns$': path.resolve(VENDOR_MODULES, 'datatables.net-fixedcolumns'),
            'dropzone$': path.resolve(VENDOR_MODULES, 'dropzone/dist/dropzone-amd-module'),
            // Alias to the full compiler build (dist/handlebars.js), not the runtime-only
            // lib/index.js `main`, since some templates are compiled in the browser.
            'handlebars$': path.resolve(VENDOR_MODULES, 'handlebars/dist/handlebars.js'),
            // qs's node `main` is the server build; the browser dist build is needed here.
            'qs$': path.resolve(VENDOR_MODULES, 'qs/dist/qs'),
            'metisMenu$': path.resolve(VENDOR_MODULES, 'metismenu'),
            // moment's default `main` lacks bundled locale data.
            'moment$': path.resolve(VENDOR_MODULES, 'moment/min/moment-with-locales'),
            'moment-timezone-with-data$': path.resolve(VENDOR_MODULES, 'moment-timezone/builds/moment-timezone-with-data'),
            'Raphael$': path.resolve(VENDOR_MODULES, 'raphael/raphael.js'),
            'typeahead$': path.resolve(VENDOR_MODULES, 'corejs-typeahead/dist/typeahead.jquery')

            // Not aliased: backbone, bowser, d3, datatables.net, datatables.net-bs,
            // underscore, uuidjs - real npm packages whose bare id and `main` field already
            // resolve to the same file the old RequireJS `paths` entry pointed at.
            // leaflet.markercluster.layersupport - ditto (bare id === npm package name and
            // `main` already matches); it require()s 'leaflet' itself via its own UMD wrapper.
        }
    },
    module: {
        rules: [
            {
                test: /\.(html|handlebars)$/,
                loader: path.resolve(__dirname, 'build/loaders/raw-text-loader.js')
            }
        ]
    },
    plugins: [
        // moment-with-locales.js contains a require('./locale') branch used only by very old
        // bundler-less environments; it's dead code here since all locales are already
        // statically bundled in. Silence the resulting "module not found" warning.
        new (require('webpack').IgnorePlugin)({
            resourceRegExp: /^\.\/locale$/,
            contextRegExp: /moment[/\\]min$/
        }),
        // Strip the RequireJS `text!`/`i18n!` loader-plugin prefixes that still remain in
        // vendor sources (the repo's own sources had `i18n!` stripped in Phase 3); `text!`
        // requires are handled below by resolving to the plain path, then raw-text-loader.
        new (require('webpack').NormalModuleReplacementPlugin)(/^text!/, (resource) => {
            resource.request = resource.request.replace(/^text!/, '');
        }),
        new (require('webpack').NormalModuleReplacementPlugin)(/^i18n!/, (resource) => {
            resource.request = resource.request.replace(/^i18n!/, '');
        }),
        // Dev-mode-only: keep idol's own non-JS static assets (and core's, which used to be
        // kept live by grunt-sync/watch) refreshed under target/classes/static without a
        // full `mvn install`. Skipped in production mode, where the real Maven
        // resource/unpack executions already populate these directories as part of a full
        // build.
        ...(isProduction ? [] : [
            new CopyWebpackPlugin({
                patterns: [
                    { from: '../core/src/main/public/static/find-favicon.ico', to: 'find-favicon.ico' },
                    { from: '../core/src/main/public/static/fonts', to: 'fonts' },
                    { from: '../core/src/main/public/static/img', to: 'img' },
                    { from: 'src/main/public/static/img', to: 'img' },
                    { from: 'src/main/public/static/css', to: 'css' },
                    { from: 'src/main/public/static/html', to: 'html' },
                    // idol's own JS tree, copied straight on top of the merged tree from the
                    // last `mvn install`/`compile` (which is what the `entry`/`resolve` above
                    // read from). This is what lets editing idol's own JS take effect on the
                    // next `npm run watch` recompile without a `mvn compile` in between -
                    // core's files aren't touched here, so a plain `mvn install -pl core` is
                    // still required to pick up core-side JS edits. Safe even where an
                    // idol-owned file (e.g. find/app/vent.js) sits alongside a core-owned
                    // sibling it `require()`s by relative path (e.g. find/app/core-vent.js) -
                    // this only overlays idol's own files, it never deletes core's.
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
        // Single non-code-split bundles are expected here, matching the current r.js output
        // (see baseline sizes captured in Phase 2); code splitting is out of scope for this
        // migration.
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
