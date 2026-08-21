'use strict';

// window.jQuery / window.$ - see amd-to-cjs-find.md correction 1: jQuery 3.7.1's UMD sees
// `module` under webpack and takes the CJS branch (noGlobal=true), setting neither global.
// icheck (window.jQuery||window.Zepto), chosen ($ = jQuery), bootstrap 3 (}(jQuery)), flot,
// metisMenu's fallback, and any admin-supplied Handlebars result template containing $(...)
// all depend on these globals existing. Everything below this shim in the require graph
// depends on it running first.
const $ = require('jquery/dist/jquery');

window.jQuery = window.$ = $;

module.exports = $;
