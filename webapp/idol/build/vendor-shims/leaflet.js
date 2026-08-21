'use strict';

// Requires leaflet.notransform first - it sets window.L_DISABLE_3D, which Leaflet reads at
// eval time (see amd-to-cjs-find.md correction 2). Dropping this silently re-enables 3D
// transforms and breaks the map image/PPTX export. leaflet.notransform is referenced from
// nowhere else in the repo.
require('leaflet.notransform');

module.exports = require('leaflet/dist/leaflet-src');
