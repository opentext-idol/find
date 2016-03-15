Maps of document location metadata can be displayed in the UI if enabled in the application configuration. The map configuration consists of two properties, a tile URL template and an attribution. The attribution is optional text rendered in the bottom right corner of the map (for example, "© Awesome Maps 2016").

The tile server URL template is the full Slippy Map Tilenames (XYZ) URL for requesting a tile from an accessible tile server, with the x, y and z coordinates replaced with curly brace variables. For example:

https://<span></span>maps.example.com/tiles/{z}/{x}/{y}.png

Any image file type supported by the browser can be used. There are many compatible tile servers available as a service over the internet, including most Open Street Map servers. However, beware of license and usage restrictions. In particular, the tile server provided by OpenStreetMap itself should not be used except for limited testing purposes.