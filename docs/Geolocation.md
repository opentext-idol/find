Maps of document location metadata can be displayed in the UI if enabled in the application configuration. The map configuration consists of several properties, detailed below.

You must configure a tile server to use geolocation. Any image file type supported by the browser can be used. There are many compatible tile servers available as a service over the internet, including most Open Street Map servers. However, beware of license and usage restrictions. In particular, the tile server provided by OpenStreetMap itself should not be used except for limited testing purposes.

Map settings are configured in the "map" section of the config.json file. An example configuration is shown below.

```
"map": {
    "enabled": true,
    "attribution": "Bob's amazing maps",
    "tileUrlTemplate": "https://{s}maps.example.com/tiles/{z}/{x}/{y}.png",
    "resultsStep": 2500,
    "initialLocation": {
        "latitude": 1.10266,
        "longitude":  -1.10254
    },
    "locationFields": [{
        "displayName": "DefaultLocation",
        "latitudeField": "LATITUDE",
        "longitudeField": "LONGITUDE",
        "iconName": "hp-settings",
        "iconColor": "black",
        "markerColor": "red"
    }]
}
```
## Main Section
- **enabled** - either `true` or `false` to enable or disable geolocation functionality.
- **attribution** - The attribution is an optional text or HTML string rendered in the bottom right corner of the map (for example, "© Awesome Maps 2016"). This may be required by your tile server provider.
- **tileUrlTemplate** - The tile server URL template is the full Slippy Map Tilenames (SXYZ) URL for requesting a tile from an accessible tile server, with the x, y and z coordinates and s server replaced with curly brace variables. For example: `https://{s}maps.example.com/tiles/{z}/{x}/{y}.png`.
- **resultsStep** - If using HPE IDOL this is the amount of results to load in each iteration of the search.

## initialLocation
This sets the location that the map will centre to while results are being loaded.
- **latitude** - The latitude of where the map should be centred when first loaded.  
- **longitude** - The longitude of where the map should be centred when first loaded.  

## locationFields
An array specifying data sets to display on the map. Each set specifies which IDOL fields contain the longitude and latitude, and how they should be displayed. The location fields used must be also specified in the `fieldsInfo` section of the configuration with the same name as they have here. See [here](https://github.com/hpe-idol/find/wiki/Field-usage-in-Find-10.11-for-IDOL).

- **displayName** - The name the user will see for this location field.    
- **latitudeField** - The IDOL field that will be used for the latitude.    
- **longitudeField** - The IDOL field that will be used for the longitude.  
- **iconName** - The name of the glyphicon to be used for the marker.  
- **iconColor** - Sets the color of the glyphicon inside the marker, must be either `black` or `white`.  
- **markerColor** - Sets the colour of the map marker, this must be one of: `red`, `darkred`, `orange`, `green`, `darkgreen`, `blue`, `purple`, `darkpuple`, `cadetblue`.