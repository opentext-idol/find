define([
    'leaflet', 'leaflet.draw', 'i18n!find/nls/bundle', 'leaflet.draw.polygonSpatial', 'leaflet.draw.negate'
], function(leaflet, leafletDraw, i18n){

    function updateTranslations(prefix, mappedPrefix, obj) {
        _.each(obj, function(val, key){
            const path = prefix + '.' + key;
            const mappedPath = mappedPrefix + '.' + key;

            switch (typeof val) {
                case 'object':
                    updateTranslations(path, mappedPath, val);
                    break;
                case 'string':
                    const translation = i18n[mappedPath];
                    if (translation != null) {
                        obj[key] = translation;
                    }
                    break;
            }
        })
    }

    // Set the Leaflet.draw strings to the corresponding key in the translation bundle, if available.
    updateTranslations('leaflet.drawLocal', 'search.geography', leaflet.drawLocal);

    return leafletDraw;
})