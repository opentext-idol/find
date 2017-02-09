define([], function(){
    // Disables 3d transforms on leaflet, since they interfere with proper operation of html2canvas in Firefox/IE11
    //   which can be observed by zooming in on the map, panning, then exporting the map as a PPT
    if (!/Chrome/.test(navigator.userAgent)) {
        window.L_DISABLE_3D=true
    }
})