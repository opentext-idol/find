'use strict';

// 0.4.1 assigns window.html2canvas and exports nothing itself, so re-export it here.
require('html2canvas/build/html2canvas');

module.exports = window.html2canvas;
