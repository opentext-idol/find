/*
 * Copyright 2017-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore'
], function(_) {

    return function(value, size) {
        return value && String(value).replace(/(\/wikipedia\/(?:commons|\w{2})\/)([^/]+\/[^/]+\/([^/]+\.(jpg|jpeg|gif|png|svg)))/i, function(all, first, second, filename, extension){
            const url = first + 'thumb/' + second + '/' + (size||300) + 'px-' + filename;

            if (extension && extension.toLowerCase() === 'svg') {
                // The thumbnailer only can raster bitmap outputs, so if we have a svg, we need to convert to e.g. png.
                return url + '.png';
            }

            return url;
        });
    };

});
