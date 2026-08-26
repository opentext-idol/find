'use strict';

// window.jQuery / window.$ - jQuery 3.7.1's UMD sees `module` under webpack and takes the CJS branch (noGlobal=true),
// setting neither global.
const $ = require('jquery/dist/jquery');

window.jQuery = window.$ = $;

module.exports = $;
