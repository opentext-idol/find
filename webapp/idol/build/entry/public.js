'use strict';

// Temporary Phase-4 stand-in for idol/src/main/public/static/js/public.js, which still uses
// the two-phase `require(['require-config'], function() { require([...]); })` bootstrap that
// webpack would turn into an async chunk. Deleted in Phase 5 once the real entry file is
// rewritten to this same shape.
const App = require('find/idol/app/idol-app');

new App();
