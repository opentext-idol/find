define([], function () {
    // Disables 3d transforms on leaflet, since they interfere with proper operation of html2canvas in Firefox/IE11
    // which can be observed by zooming in on the map, panning, then exporting the map as a PPTX
    // After upgrading from Leaflet 0.7.7 to 1.0.3 ; Chrome also needs this flag.
    window.L_DISABLE_3D = true;
});