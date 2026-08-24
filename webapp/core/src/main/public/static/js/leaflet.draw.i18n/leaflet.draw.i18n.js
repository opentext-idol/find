const _ = require('underscore');
const leaflet = require('leaflet');
const leafletDraw = require('leaflet.draw');
const i18n = require('find/nls/bundle');

// Loaded for side effects only - do not remove.
require('leaflet.draw.polygonSpatial');
require('leaflet.draw.negate');

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

module.exports = leafletDraw;

